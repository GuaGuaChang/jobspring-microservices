package com.jobspring.user.service;

import com.jobspring.user.client.AuthUserClient;
import com.jobspring.user.dto.PromoteToHrRequest;
import com.jobspring.user.dto.UserBrief;
import com.jobspring.user.dto.UserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    @DisplayName("batchBrief：保持原请求顺序，去重后查询，未知ID填(unknown)")
    void batchBrief_mergeAndFillUnknown() {
        AuthUserClient client = mock(AuthUserClient.class);
        UserService service = new UserService(client);

        // 下游只会被调用一次，且只带去重后的 [3,1,2]
        when(client.batchAccountBriefs(eq(List.of(3L,1L,2L))))
                .thenReturn(List.of(
                        new AuthUserClient.AccountBrief(1L, "User-1"),
                        new AuthUserClient.AccountBrief(3L, "User-3")
                ));

        List<UserBrief> result = service.batchBrief(List.of(3L, 1L, 3L, 2L));

        assertThat(result).containsExactly(
                new UserBrief(3L, "User-3"),
                new UserBrief(1L, "User-1"),
                new UserBrief(3L, "User-3"),
                new UserBrief(2L, "(unknown)")
        );

        verify(client, times(1)).batchAccountBriefs(eq(List.of(3L,1L,2L)));
        verifyNoMoreInteractions(client);
    }

    @Test
    @DisplayName("makeHr：委托给下游 AuthUserClient.promoteToHr")
    void makeHr_delegate() {
        AuthUserClient client = mock(AuthUserClient.class);
        UserService service = new UserService(client);

        PromoteToHrRequest req = new PromoteToHrRequest(); // 字段不关心
        service.makeHr(99L, req);

        verify(client).promoteToHr(99L, req);
        verifyNoMoreInteractions(client);
    }

    @Test
    @DisplayName("getUserById：委托给下游 AuthUserClient.getUserById")
    void getUserById_delegate() {
        AuthUserClient client = mock(AuthUserClient.class);
        UserService service = new UserService(client);

        when(client.getUserById(7L)).thenReturn(new UserDTO());

        UserDTO dto = service.getUserById(7L);
        assertThat(dto).isNotNull();

        verify(client).getUserById(7L);
        verifyNoMoreInteractions(client);
    }
}
