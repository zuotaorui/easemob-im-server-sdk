package com.easemob.im.server.api.group.settings;

import com.easemob.im.server.api.Context;
import com.easemob.im.server.api.DefaultErrorMapper;
import com.easemob.im.server.api.ErrorMapper;
import com.easemob.im.server.exception.EMUnknownException;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class UpdateGroup {

    private Context context;

    public UpdateGroup(Context context) {
        this.context = context;
    }

    public Mono<Void> update(String groupId, Consumer<UpdateGroupRequest> customizer) {
        UpdateGroupRequest request = new UpdateGroupRequest();
        customizer.accept(request);

        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient.put()
                        .uri(String.format("/chatgroups/%s", groupId))
                        .send(Mono.create(sink -> sink
                                .success(this.context.getCodec().encode(request))))
                        .responseSingle(
                                (rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(buf -> this.context.getCodec().decode(buf, UpdateGroupResponse.class))
                .doOnNext(rsp -> {
                    if (request.getMaxMembers() != null && (rsp.getMaxMembersUpdated() == null
                            || !rsp.getMaxMembersUpdated())) {
                        throw new EMUnknownException("maxMembers");
                    }

                    if (request.getCanMemberInviteOthers() != null && (
                            rsp.getMemberCanInviteOthersUpdated() == null || !rsp
                                    .getMemberCanInviteOthersUpdated())) {
                        throw new EMUnknownException("memberCanInviteOthers");
                    }

                    if (request.getNeedApproveToJoin() != null && (
                            rsp.getNeedApproveToJoinUpdated() == null || !rsp
                                    .getNeedApproveToJoinUpdated())) {
                        throw new EMUnknownException("needApproveToJoin");
                    }
                })
                .then();
    }

    public Mono<Void> updateOwner(String groupId, String owner) {
        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient.put()
                        .uri(String.format("/chatgroups/%s", groupId))
                        .send(Mono.create(sink -> sink.success(this.context.getCodec()
                                .encode(new UpdateGroupOwnerRequest(owner)))))
                        .responseSingle(
                                (rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .then();
    }
}
