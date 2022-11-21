# CID - Java SDK

> java sdk for cid

## Configuration

The SDK will detect the endpoint (unix socket or http api) automatically when initialized as part of a action or workflow run based on the follwing envionrment properties:

| Property       | Description                                  |
|----------------|----------------------------------------------|
| CID_API_SOCKET | unix socket file                             |
| CID_API_ADDR   | http endpoint                                |
| CID_API_SECRET | temporary api key to securely access the api |

For local testing run `cid api` and set `CID_API_ADDR` to `http://localhost:7400`.

## Code Example

## License

Released under the [MIT License](./LICENSE).
