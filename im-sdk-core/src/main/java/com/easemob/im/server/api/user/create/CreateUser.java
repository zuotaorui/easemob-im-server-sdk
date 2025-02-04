package com.easemob.im.server.api.user.create;

import com.easemob.im.server.api.Context;
import com.easemob.im.server.api.DefaultErrorMapper;
import com.easemob.im.server.api.ErrorMapper;
import com.easemob.im.server.api.user.get.UserGetResponse;
import com.easemob.im.server.model.EMUser;
import reactor.core.publisher.Mono;

public class CreateUser {

    private Context context;

    public CreateUser(Context context) {
        this.context = context;
    }

    public Mono<EMUser> single(String username, String password) {
        return this.context.getHttpClient()
                .flatMap(httpClient -> httpClient.post()
                        .uri("/users")
                        .send(Mono.create(sink -> sink.success(this.context.getCodec()
                                .encode(new CreateUserRequest(username, password)))))
                        .responseSingle(
                                (rsp, buf) -> Mono.zip(Mono.just(rsp), buf)))
                .map(tuple2 -> {
                    ErrorMapper mapper = new DefaultErrorMapper();
                    mapper.statusCode(tuple2.getT1());
                    mapper.checkError(tuple2.getT2());

                    return tuple2.getT2();
                })
                .map(byteBuf -> {
                    UserGetResponse userGetResponse =
                            this.context.getCodec().decode(byteBuf, UserGetResponse.class);
                    return userGetResponse.getEMUser(username.toLowerCase());
                });
    }

}
