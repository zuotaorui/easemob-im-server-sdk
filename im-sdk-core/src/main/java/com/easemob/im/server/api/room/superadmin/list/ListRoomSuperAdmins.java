package com.easemob.im.server.api.room.superadmin.list;

import com.easemob.im.server.api.Context;
import com.easemob.im.server.api.DefaultErrorMapper;
import com.easemob.im.server.api.ErrorMapper;
import com.easemob.im.server.api.room.member.list.ListRoomMembersResponseV1;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ListRoomSuperAdmins {

    private Context context;

    public ListRoomSuperAdmins(Context context) {
        this.context = context;
    }

    public Mono<ListRoomSuperAdminsResponse> next(int pageNum, int pageSize) {
        String uri =
                String.format("/chatrooms/super_admin?pagenum=%d&pagesize=%d", pageNum, pageSize);

        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient.get()
                        .uri(uri)
                        .responseSingle(
                                (rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(buf -> this.context.getCodec().decode(buf, ListRoomSuperAdminsResponse.class));
    }

    public Flux<String> all(int pageSize) {
        return next(1, pageSize)
                .expand(rsp -> {
                    return rsp.getAdmins().size() < pageSize ?
                            Mono.empty() :
                            next(Integer.parseInt(rsp.getParamsInfo().getPageNum()) + 1, pageSize);
                })
                .concatMapIterable(ListRoomSuperAdminsResponse::getAdmins);
    }
}
