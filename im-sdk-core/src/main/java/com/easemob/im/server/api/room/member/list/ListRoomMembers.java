package com.easemob.im.server.api.room.member.list;

import com.easemob.im.server.api.Context;
import com.easemob.im.server.api.DefaultErrorMapper;
import com.easemob.im.server.api.ErrorMapper;
import com.easemob.im.server.model.EMPage;
import io.netty.handler.codec.http.QueryStringEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ListRoomMembers {

    private Context context;

    public ListRoomMembers(Context context) {
        this.context = context;
    }

    public Flux<String> all(String roomId, int limit, String sort) {
        return next(roomId, limit, null, sort)
                .expand(rsp -> rsp.getCursor() == null ?
                        Mono.empty() :
                        next(roomId, limit, rsp.getCursor(), sort))
                .concatMapIterable(EMPage::getValues);
    }

    public Mono<EMPage<String>> next(String roomId, int limit, String cursor, String sort) {
        final String uriPath = String.format("/chatrooms/%s/users", roomId);
        QueryStringEncoder encoder = new QueryStringEncoder(uriPath);
        encoder.addParam("version", "v3");
        encoder.addParam("limit", String.valueOf(limit));
        if (cursor != null) {
            encoder.addParam("cursor", cursor);
        }
        if (sort != null) {
            encoder.addParam("sort", sort);
        }
        String uriString = encoder.toString();
        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient.get()
                        .uri(uriString)
                        .responseSingle(
                                (rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(buf -> this.context.getCodec().decode(buf, ListRoomMembersResponse.class))
                .map(ListRoomMembersResponse::toEMPage);
    }

    public Flux<Map<String, String>> all(String roomId, int pageSize) {
        return next(roomId, 1, pageSize)
                .expand(rsp -> {
                    return rsp.getMemberCount() < pageSize ?
                        Mono.empty() :
                        next(roomId,  Integer.parseInt(rsp.getParamsInfo().getPageNum()) + 1, pageSize);
                })
                .concatMapIterable(ListRoomMembersResponseV1::getMembers);
    }

    public Mono<ListRoomMembersResponseV1> next(String roomId, int pageNum, int pageSize) {
        final String uriPath = String.format("/chatrooms/%s/users", roomId);
        QueryStringEncoder encoder = new QueryStringEncoder(uriPath);
        encoder.addParam("pagenum", String.valueOf(pageNum));
        encoder.addParam("pagesize", String.valueOf(pageSize));

        String uriString = encoder.toString();
        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient.get()
                        .uri(uriString)
                        .responseSingle(
                                (rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(buf -> this.context.getCodec().decode(buf, ListRoomMembersResponseV1.class));
    }
}
