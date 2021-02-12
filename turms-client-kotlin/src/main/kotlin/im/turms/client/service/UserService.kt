/*
 * Copyright (C) 2019 The Turms Project
 * https://github.com/turms-im/turms
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.turms.client.service

import com.google.protobuf.*
import im.turms.client.TurmsClient
import im.turms.client.annotation.NotEmpty
import im.turms.client.constant.TurmsStatusCode
import im.turms.client.exception.TurmsBusinessException
import im.turms.client.model.SessionCloseInfo
import im.turms.client.model.User
import im.turms.client.model.UserInfoWithVersion
import im.turms.client.model.UserLocation
import im.turms.client.util.SystemUtil
import im.turms.common.constant.DeviceType
import im.turms.common.constant.ProfileAccessStrategy
import im.turms.common.constant.ResponseAction
import im.turms.common.constant.UserStatus
import im.turms.common.constant.statuscode.SessionCloseStatus
import im.turms.common.model.bo.common.Int64ValuesWithVersion
import im.turms.common.model.bo.user.*
import im.turms.common.model.dto.notification.TurmsNotification
import im.turms.common.model.dto.request.user.*
import im.turms.common.model.dto.request.user.relationship.*
import im.turms.common.util.Validator
import java.util.*

/**
 * @author James Chen
 */
class UserService(private val turmsClient: TurmsClient) {

    var userInfo: User? = null
    private var storePassword = false

    private val onOnlineListeners: MutableList<(() -> Unit)> = LinkedList()
    private val onOfflineListeners: MutableList<((SessionCloseInfo?) -> Unit)> = LinkedList()

    val isLoggedIn: Boolean get() = turmsClient.driver.stateStore.isSessionOpen

    init {
        turmsClient.driver.addOnDisconnectedListener { changeToOffline() }
        turmsClient.driver.addNotificationListener {
            if (it.hasCloseStatus() && isLoggedIn) {
                val info = SessionCloseInfo(
                    it.closeStatus.value,
                    if (it.hasCode()) it.code.value else null,
                    if (it.hasReason()) it.reason.value else null,
                )
                changeToOffline(info)
            }
        }
    }

    suspend fun login(
        userId: Long,
        password: String? = null,
        deviceType: DeviceType = SystemUtil.deviceType,
        onlineStatus: UserStatus = UserStatus.AVAILABLE,
        location: UserLocation? = null,
        storePassword: Boolean = false
    ) {
        val user = User(userId)
        if (storePassword) {
            user.password = password
        }
        user.deviceType = deviceType
        user.onlineStatus = onlineStatus
        user.location = location
        if (!turmsClient.driver.isConnected) {
            turmsClient.driver.connect()
        }
        turmsClient.driver.send(CreateSessionRequest.newBuilder().apply {
            this.userId = userId
            password?.let { this.password = StringValue.of(it) }
            deviceType.let { this.deviceType = it }
            onlineStatus.let { this.userStatus = it }
            location?.let {
                this.location = im.turms.common.model.bo.user.UserLocation.newBuilder().apply {
                    this.longitude = it.longitude
                    this.latitude = it.latitude
                }.build()
            }
        })
        userInfo = user
        this.storePassword = storePassword
        changeToOnline()
    }

    suspend fun logout(disconnect: Boolean = true) {
        if (disconnect) {
            turmsClient.driver.disconnect()
        } else {
            turmsClient.driver.send(DeleteSessionRequest.newBuilder())
        }
        changeToOffline(SessionCloseInfo(SessionCloseStatus.DISCONNECTED_BY_CLIENT.code))
    }

    suspend fun updateOnlineStatus(onlineStatus: UserStatus) =
        if (onlineStatus == UserStatus.OFFLINE) {
            throw TurmsBusinessException(
                TurmsStatusCode.ILLEGAL_ARGUMENT,
                "The online status must not be $onlineStatus"
            )
        } else turmsClient.driver
            .send(
                UpdateUserOnlineStatusRequest.newBuilder().apply {
                    this.userStatus = onlineStatus
                }
            ).run {
                userInfo?.onlineStatus = onlineStatus
            }

    suspend fun disconnectOnlineDevices(@NotEmpty deviceTypes: Set<DeviceType>) =
        if (deviceTypes.isEmpty()) {
            throw TurmsBusinessException(TurmsStatusCode.ILLEGAL_ARGUMENT, "deviceTypes must not be null or empty")
        } else turmsClient.driver
            .send(
                UpdateUserOnlineStatusRequest.newBuilder().apply {
                    this.userStatus = UserStatus.OFFLINE
                    this.addAllDeviceTypes(deviceTypes)
                }
            ).run { }

    suspend fun updatePassword(password: String) = turmsClient.driver
        .send(
            UpdateUserRequest.newBuilder().apply {
                this.password = StringValue.of(password)
            }
        ).run {
            if (storePassword) {
                userInfo?.password = password
            }
        }

    suspend fun updateProfile(
        name: String? = null,
        intro: String? = null,
        profileAccessStrategy: ProfileAccessStrategy? = null
    ) {
        if (!Validator.areAllNull(name, intro, profileAccessStrategy)) {
            return turmsClient.driver
                .send(
                    UpdateUserRequest.newBuilder().apply {
                        name?.let { this.name = StringValue.of(it) }
                        intro?.let { this.intro = StringValue.of(it) }
                        profileAccessStrategy?.let { this.profileAccessStrategy = it }
                    }
                ).run {}
        }
    }

    suspend fun queryUserProfile(
        userId: Long,
        lastUpdatedDate: Date? = null
    ): UserInfoWithVersion? = turmsClient.driver
        .send(
            QueryUserProfileRequest.newBuilder().apply {
                this.userId = userId
                lastUpdatedDate?.let { this.lastUpdatedDate = Int64Value.of(it.time) }
            }
        ).let { UserInfoWithVersion.from(it) }

    suspend fun queryUserIdsNearby(
        latitude: Float,
        longitude: Float,
        distance: Float? = null,
        maxNumber: Int? = null
    ): List<Long> = turmsClient.driver
        .send(
            QueryUserIdsNearbyRequest.newBuilder().apply {
                this.latitude = latitude
                this.longitude = longitude
                distance?.let { this.distance = FloatValue.of(it) }
                maxNumber?.let { this.maxNumber = Int32Value.of(it) }
            }
        ).data.idsWithVersion.valuesList

    suspend fun queryUserSessionIdsNearby(
        latitude: Float,
        longitude: Float,
        distance: Float? = null,
        maxNumber: Int? = null
    ): List<UserSessionId> = turmsClient.driver
        .send(
            QueryUserIdsNearbyRequest.newBuilder().apply {
                this.latitude = latitude
                this.longitude = longitude
                distance?.let { this.distance = FloatValue.of(it) }
                maxNumber?.let { this.maxNumber = Int32Value.of(it) }
            }
        ).data.userSessionIds.userSessionIdsList

    suspend fun queryUserInfosNearby(
        latitude: Float,
        longitude: Float,
        distance: Float? = null,
        maxNumber: Int? = null
    ): List<UserInfo> = turmsClient.driver
        .send(
            QueryUserIdsNearbyRequest.newBuilder().apply {
                this.latitude = latitude
                this.longitude = longitude
                distance?.let { this.distance = FloatValue.of(it) }
                maxNumber?.let { this.maxNumber = Int32Value.of(it) }
            }
        ).data.usersInfosWithVersion.userInfosList

    suspend fun queryOnlineStatusesRequest(@NotEmpty userIds: Set<Long>): List<UserStatusDetail> =
        if (userIds.isEmpty()) {
            throw TurmsBusinessException(TurmsStatusCode.ILLEGAL_ARGUMENT, "userIds must not be null or empty")
        } else turmsClient.driver
            .send(
                QueryUserOnlineStatusesRequest.newBuilder().apply {
                    addAllUserIds(userIds)
                }
            ).data.usersOnlineStatuses.userStatusesList

    // Relationship
    suspend fun queryRelationships(
        relatedUserIds: Set<Long>? = null,
        isBlocked: Boolean? = null,
        groupIndex: Int? = null,
        lastUpdatedDate: Date? = null
    ): UserRelationshipsWithVersion? = turmsClient.driver
        .send(
            QueryRelationshipsRequest.newBuilder().apply {
                relatedUserIds?.let { addAllUserIds(it) }
                isBlocked?.let { this.blocked = BoolValue.of(it) }
                groupIndex?.let { this.groupIndex = Int32Value.of(it) }
                lastUpdatedDate?.let { this.lastUpdatedDate = Int64Value.of(it.time) }
            }
        ).let {
            val data: TurmsNotification.Data = it.data
            if (data.hasUserRelationshipsWithVersion()) data.userRelationshipsWithVersion else null
        }

    suspend fun queryRelatedUserIds(
        isBlocked: Boolean? = null,
        groupIndex: Int? = null,
        lastUpdatedDate: Date? = null
    ): Int64ValuesWithVersion? = turmsClient.driver
        .send(
            QueryRelatedUserIdsRequest.newBuilder().apply {
                isBlocked?.let { this.blocked = BoolValue.of(it) }
                groupIndex?.let { this.groupIndex = Int32Value.of(it) }
                lastUpdatedDate?.let { this.lastUpdatedDate = Int64Value.of(it.time) }
            }
        ).let {
            val data: TurmsNotification.Data = it.data
            if (data.hasIdsWithVersion()) data.idsWithVersion else null
        }

    suspend fun queryFriends(
        groupIndex: Int? = null,
        lastUpdatedDate: Date? = null
    ): UserRelationshipsWithVersion? =
        queryRelationships(isBlocked = false, groupIndex = groupIndex, lastUpdatedDate = lastUpdatedDate)

    suspend fun queryBlockedUsers(
        groupIndex: Int? = null,
        lastUpdatedDate: Date? = null
    ): UserRelationshipsWithVersion? =
        queryRelationships(isBlocked = true, groupIndex = groupIndex, lastUpdatedDate = lastUpdatedDate)

    suspend fun createRelationship(
        userId: Long,
        isBlocked: Boolean,
        groupIndex: Int? = null
    ) = turmsClient.driver
        .send(
            CreateRelationshipRequest.newBuilder().apply {
                this.userId = userId
                this.blocked = isBlocked
                groupIndex?.let { this.groupIndex = Int32Value.of(it) }
            }
        ).run {}

    suspend fun createFriendRelationship(
        userId: Long,
        groupIndex: Int? = null
    ) = createRelationship(userId, false, groupIndex)

    suspend fun createBlockedUserRelationship(
        userId: Long,
        groupIndex: Int? = null
    ) = createRelationship(userId, true, groupIndex)

    suspend fun deleteRelationship(
        relatedUserId: Long,
        deleteGroupIndex: Int? = null,
        targetGroupIndex: Int? = null
    ) = turmsClient.driver
        .send(
            DeleteRelationshipRequest.newBuilder().apply {
                this.userId = relatedUserId
                deleteGroupIndex?.let { this.groupIndex = Int32Value.of(it) }
                targetGroupIndex?.let { this.targetGroupIndex = Int32Value.of(it) }
            }
        ).run {}

    suspend fun updateRelationship(
        relatedUserId: Long,
        isBlocked: Boolean? = null,
        groupIndex: Int? = null
    ) {
        if (!Validator.areAllFalsy(isBlocked, groupIndex)) {
            return turmsClient.driver
                .send(
                    UpdateRelationshipRequest.newBuilder().apply {
                        this.userId = relatedUserId
                        isBlocked?.let { this.blocked = BoolValue.of(it) }
                        groupIndex?.let { this.newGroupIndex = Int32Value.of(it) }
                    }
                ).run {}
        }
    }

    suspend fun sendFriendRequest(
        recipientId: Long,
        content: String
    ): Long = turmsClient.driver
        .send(
            CreateFriendRequestRequest.newBuilder().apply {
                this.recipientId = recipientId
                this.content = content
            }
        ).data.ids.getValues(0)

    suspend fun replyFriendRequest(
        requestId: Long,
        responseAction: ResponseAction,
        reason: String? = null
    ) = turmsClient.driver
        .send(
            UpdateFriendRequestRequest.newBuilder().apply {
                this.requestId = requestId
                this.responseAction = responseAction
                reason?.let { this.reason = StringValue.of(it) }
            }
        ).run {}

    suspend fun queryFriendRequests(
        areSentByMe: Boolean,
        lastUpdatedDate: Date? = null
    ): UserFriendRequestsWithVersion? = turmsClient.driver
        .send(
            QueryFriendRequestsRequest.newBuilder().apply {
                this.areSentByMe = areSentByMe
                lastUpdatedDate?.let { this.lastUpdatedDate = Int64Value.of(it.time) }
            }
        ).let {
            val data: TurmsNotification.Data = it.data
            if (data.hasUserFriendRequestsWithVersion()) data.userFriendRequestsWithVersion else null
        }

    suspend fun createRelationshipGroup(name: String): Int = turmsClient.driver
        .send(
            CreateRelationshipGroupRequest.newBuilder().apply {
                this.name = name
            }
        ).data.ids.getValues(0).toInt()

    suspend fun deleteRelationshipGroups(
        groupIndex: Int,
        targetGroupIndex: Int? = null
    ) = turmsClient.driver
        .send(
            DeleteRelationshipGroupRequest.newBuilder().apply {
                this.groupIndex = groupIndex
                targetGroupIndex?.let { this.targetGroupIndex = Int32Value.of(it) }
            }
        ).run {}

    suspend fun updateRelationshipGroup(
        groupIndex: Int,
        newName: String
    ) = turmsClient.driver
        .send(
            UpdateRelationshipGroupRequest.newBuilder().apply {
                this.groupIndex = groupIndex
                this.newName = newName
            }
        ).run {}

    suspend fun queryRelationshipGroups(lastUpdatedDate: Date? = null): UserRelationshipGroupsWithVersion? =
        turmsClient.driver
            .send(
                QueryRelationshipGroupsRequest.newBuilder().apply {
                    lastUpdatedDate?.let { this.lastUpdatedDate = Int64Value.of(it.time) }
                }
            ).let {
                val data: TurmsNotification.Data = it.data
                if (data.hasUserRelationshipGroupsWithVersion()) data.userRelationshipGroupsWithVersion else null
            }

    suspend fun moveRelatedUserToGroup(
        relatedUserId: Long,
        groupIndex: Int
    ) = turmsClient.driver
        .send(
            UpdateRelationshipRequest.newBuilder().apply {
                this.userId = relatedUserId
                this.newGroupIndex = Int32Value.of(groupIndex)
            }
        ).run {}

    /**
     * updateLocation() in UserService is different from sendMessage() with records of location in MessageService
     * updateLocation() in UserService sends the location of user to the server only.
     * sendMessage() with records of location sends user's location to both server and its recipients.
     */
    suspend fun updateLocation(
        latitude: Float,
        longitude: Float,
        name: String? = null,
        address: String? = null
    ) = turmsClient.driver
        .send(
            UpdateUserLocationRequest.newBuilder().apply {
                this.latitude = latitude
                this.longitude = longitude
                name?.let { this.name = StringValue.of(it) }
                address?.let { this.address = StringValue.of(it) }
            }
        ).run {}

    private fun changeToOnline() {
        if (!isLoggedIn) {
            turmsClient.driver.stateStore.isSessionOpen = true
            onOnlineListeners.forEach { it() }
        }
    }

    private fun changeToOffline(sessionCloseInfo: SessionCloseInfo? = null) {
        if (isLoggedIn) {
            userInfo?.onlineStatus = UserStatus.OFFLINE
            turmsClient.driver.stateStore.isSessionOpen = false
            onOfflineListeners.forEach { it(sessionCloseInfo) }
        }
    }

}