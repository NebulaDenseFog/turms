import Foundation

public struct Response<T> {
    public let requestId: Int64?
    public let code: Int
    public let data: T

    init(requestId: Int64? = nil, code: Int, data: T = () as! T) {
        self.requestId = requestId
        self.code = code
        self.data = data
    }

    static func value(_ data: T) -> Response<T> {
        return Response(code: ResponseStatusCode.ok.rawValue, data: data)
    }

    static func empty() -> Response<T> {
        return Response(code: ResponseStatusCode.ok.rawValue, data: () as! T)
    }

    static func emptyArray() -> Response<T> {
        return Response(code: ResponseStatusCode.ok.rawValue, data: [] as! T)
    }

    static func fromNotification(_ notification: TurmsNotification, _ dataTransformer: ((TurmsNotification.DataMessage) throws -> T)? = nil) throws -> Response<T> {
        if !notification.hasCode {
            throw ResponseError(.invalidNotification, "Cannot parse a success response from a notification without code")
        }
        if notification.isError {
            throw ResponseError(
                .invalidNotification,
                "Cannot parse a success response from non-success notification"
            )
        }
        var data: T
        do {
            data = try dataTransformer?(notification.data) ?? () as! T
        } catch {
            throw ResponseError(
                .invalidNotification,
                "Failed to transform notification data: \(notification.data). Error: \(error)"
            )
        }
        return Response(
            requestId: notification.hasRequestID ? notification.requestID : nil,
            code: Int(notification.code),
            data: data
        )
    }
}