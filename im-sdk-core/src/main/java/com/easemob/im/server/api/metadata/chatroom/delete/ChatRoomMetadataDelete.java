package com.easemob.im.server.api.metadata.chatroom.delete;

import com.easemob.im.server.EMException;
import com.easemob.im.server.api.Context;
import com.easemob.im.server.api.DefaultErrorMapper;
import com.easemob.im.server.api.ErrorMapper;
import com.easemob.im.server.api.metadata.chatroom.ChatRoomMetadataRequest;
import com.easemob.im.server.exception.EMUnknownException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;

import java.util.List;

public class ChatRoomMetadataDelete {

    private Context context;

    public ChatRoomMetadataDelete(Context context) {
        this.context = context;
    }

    public Mono<ChatRoomMetadataDeleteResponse> fromChatRoom(String operator, String chatroomId,
            List<String> keys) {
        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient
                        .headers(header -> header.add("Content-Type", "application/json")).delete()
                        .uri(String.format("/metadata/chatroom/%s/user/%s", chatroomId, operator))
                        .send(Mono.create(sink -> sink.success(this.context.getCodec()
                                .encode(new ChatRoomMetadataRequest(keys)))))
                        .responseSingle((rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(byteBuf -> {
                    return this.context.getCodec()
                    .decode(byteBuf, ChatRoomMetadataDeleteResponse.class);
                });
    }

    public Mono<ChatRoomMetadataDeleteResponse> fromChatRoomForced(String chatroomId,
            List<String> keys) {
        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient
                        .headers(header -> header.add("Content-Type", "application/json")).delete()
                        .uri(String.format("/metadata/chatroom/%s/user/admin/forced", chatroomId))
                        .send(Mono.create(sink -> sink.success(this.context.getCodec()
                                .encode(new ChatRoomMetadataRequest(keys)))))
                        .responseSingle((rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(byteBuf -> {
                    return this.context.getCodec()
                    .decode(byteBuf, ChatRoomMetadataDeleteResponse.class);
                });
    }
}
