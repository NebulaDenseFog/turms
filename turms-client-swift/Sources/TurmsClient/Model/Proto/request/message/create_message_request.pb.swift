// DO NOT EDIT.
// swift-format-ignore-file
//
// Generated by the Swift generator plugin for the protocol buffer compiler.
// Source: request/message/create_message_request.proto
//
// For information on using the generated types, please see the documentation:
//   https://github.com/apple/swift-protobuf/

import Foundation
import SwiftProtobuf

// If the compiler emits an error on this type, it is because this file
// was generated by a version of the `protoc` Swift plug-in that is
// incompatible with the version of SwiftProtobuf to which you are linking.
// Please ensure that you are building against the same version of the API
// that was used to generate this file.
private struct _GeneratedWithProtocGenSwiftVersion: SwiftProtobuf.ProtobufAPIVersionCheck {
    struct _2: SwiftProtobuf.ProtobufAPIVersion_2 {}
    typealias Version = _2
}

public struct CreateMessageRequest {
    // SwiftProtobuf.Message conformance is added in an extension below. See the
    // `Message` and `Message+*Additions` files in the SwiftProtobuf library for
    // methods supported on all messages.

    public var messageID: Int64 {
        get { return _messageID ?? 0 }
        set { _messageID = newValue }
    }

    /// Returns true if `messageID` has been explicitly set.
    public var hasMessageID: Bool { return _messageID != nil }
    /// Clears the value of `messageID`. Subsequent reads from it will return its default value.
    public mutating func clearMessageID() { _messageID = nil }

    /// is_system_message can only be true if the user is an administrator,
    /// or turms server will return an error
    public var isSystemMessage: Bool {
        get { return _isSystemMessage ?? false }
        set { _isSystemMessage = newValue }
    }

    /// Returns true if `isSystemMessage` has been explicitly set.
    public var hasIsSystemMessage: Bool { return _isSystemMessage != nil }
    /// Clears the value of `isSystemMessage`. Subsequent reads from it will return its default value.
    public mutating func clearIsSystemMessage() { _isSystemMessage = nil }

    public var groupID: Int64 {
        get { return _groupID ?? 0 }
        set { _groupID = newValue }
    }

    /// Returns true if `groupID` has been explicitly set.
    public var hasGroupID: Bool { return _groupID != nil }
    /// Clears the value of `groupID`. Subsequent reads from it will return its default value.
    public mutating func clearGroupID() { _groupID = nil }

    public var recipientID: Int64 {
        get { return _recipientID ?? 0 }
        set { _recipientID = newValue }
    }

    /// Returns true if `recipientID` has been explicitly set.
    public var hasRecipientID: Bool { return _recipientID != nil }
    /// Clears the value of `recipientID`. Subsequent reads from it will return its default value.
    public mutating func clearRecipientID() { _recipientID = nil }

    public var deliveryDate: Int64 {
        get { return _deliveryDate ?? 0 }
        set { _deliveryDate = newValue }
    }

    /// Returns true if `deliveryDate` has been explicitly set.
    public var hasDeliveryDate: Bool { return _deliveryDate != nil }
    /// Clears the value of `deliveryDate`. Subsequent reads from it will return its default value.
    public mutating func clearDeliveryDate() { _deliveryDate = nil }

    public var text: String {
        get { return _text ?? String() }
        set { _text = newValue }
    }

    /// Returns true if `text` has been explicitly set.
    public var hasText: Bool { return _text != nil }
    /// Clears the value of `text`. Subsequent reads from it will return its default value.
    public mutating func clearText() { _text = nil }

    public var records: [Data] = []

    public var burnAfter: Int32 {
        get { return _burnAfter ?? 0 }
        set { _burnAfter = newValue }
    }

    /// Returns true if `burnAfter` has been explicitly set.
    public var hasBurnAfter: Bool { return _burnAfter != nil }
    /// Clears the value of `burnAfter`. Subsequent reads from it will return its default value.
    public mutating func clearBurnAfter() { _burnAfter = nil }

    public var preMessageID: Int64 {
        get { return _preMessageID ?? 0 }
        set { _preMessageID = newValue }
    }

    /// Returns true if `preMessageID` has been explicitly set.
    public var hasPreMessageID: Bool { return _preMessageID != nil }
    /// Clears the value of `preMessageID`. Subsequent reads from it will return its default value.
    public mutating func clearPreMessageID() { _preMessageID = nil }

    public var unknownFields = SwiftProtobuf.UnknownStorage()

    public init() {}

    fileprivate var _messageID: Int64?
    fileprivate var _isSystemMessage: Bool?
    fileprivate var _groupID: Int64?
    fileprivate var _recipientID: Int64?
    fileprivate var _deliveryDate: Int64?
    fileprivate var _text: String?
    fileprivate var _burnAfter: Int32?
    fileprivate var _preMessageID: Int64?
}

// MARK: - Code below here is support for the SwiftProtobuf runtime.

private let _protobuf_package = "im.turms.proto"

extension CreateMessageRequest: SwiftProtobuf.Message, SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
    public static let protoMessageName: String = _protobuf_package + ".CreateMessageRequest"
    public static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
        1: .standard(proto: "message_id"),
        2: .standard(proto: "is_system_message"),
        3: .standard(proto: "group_id"),
        4: .standard(proto: "recipient_id"),
        5: .standard(proto: "delivery_date"),
        6: .same(proto: "text"),
        7: .same(proto: "records"),
        8: .standard(proto: "burn_after"),
        9: .standard(proto: "pre_message_id"),
    ]

    public mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
        while let fieldNumber = try decoder.nextFieldNumber() {
            // The use of inline closures is to circumvent an issue where the compiler
            // allocates stack space for every case branch when no optimizations are
            // enabled. https://github.com/apple/swift-protobuf/issues/1034
            switch fieldNumber {
            case 1: try try decoder.decodeSingularInt64Field(value: &_messageID)
            case 2: try try decoder.decodeSingularBoolField(value: &_isSystemMessage)
            case 3: try try decoder.decodeSingularInt64Field(value: &_groupID)
            case 4: try try decoder.decodeSingularInt64Field(value: &_recipientID)
            case 5: try try decoder.decodeSingularInt64Field(value: &_deliveryDate)
            case 6: try try decoder.decodeSingularStringField(value: &_text)
            case 7: try try decoder.decodeRepeatedBytesField(value: &records)
            case 8: try try decoder.decodeSingularInt32Field(value: &_burnAfter)
            case 9: try try decoder.decodeSingularInt64Field(value: &_preMessageID)
            default: break
            }
        }
    }

    public func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
        try { if let v = self._messageID {
            try visitor.visitSingularInt64Field(value: v, fieldNumber: 1)
        } }()
        try { if let v = self._isSystemMessage {
            try visitor.visitSingularBoolField(value: v, fieldNumber: 2)
        } }()
        try { if let v = self._groupID {
            try visitor.visitSingularInt64Field(value: v, fieldNumber: 3)
        } }()
        try { if let v = self._recipientID {
            try visitor.visitSingularInt64Field(value: v, fieldNumber: 4)
        } }()
        try { if let v = self._deliveryDate {
            try visitor.visitSingularInt64Field(value: v, fieldNumber: 5)
        } }()
        try { if let v = self._text {
            try visitor.visitSingularStringField(value: v, fieldNumber: 6)
        } }()
        if !records.isEmpty {
            try visitor.visitRepeatedBytesField(value: records, fieldNumber: 7)
        }
        try { if let v = self._burnAfter {
            try visitor.visitSingularInt32Field(value: v, fieldNumber: 8)
        } }()
        try { if let v = self._preMessageID {
            try visitor.visitSingularInt64Field(value: v, fieldNumber: 9)
        } }()
        try unknownFields.traverse(visitor: &visitor)
    }

    public static func == (lhs: CreateMessageRequest, rhs: CreateMessageRequest) -> Bool {
        if lhs._messageID != rhs._messageID { return false }
        if lhs._isSystemMessage != rhs._isSystemMessage { return false }
        if lhs._groupID != rhs._groupID { return false }
        if lhs._recipientID != rhs._recipientID { return false }
        if lhs._deliveryDate != rhs._deliveryDate { return false }
        if lhs._text != rhs._text { return false }
        if lhs.records != rhs.records { return false }
        if lhs._burnAfter != rhs._burnAfter { return false }
        if lhs._preMessageID != rhs._preMessageID { return false }
        if lhs.unknownFields != rhs.unknownFields { return false }
        return true
    }
}