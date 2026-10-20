package com.example.ver3_member_mvc.controller;

import com.example.ver3_member_mvc.dto.MemberRequestDto;
import com.example.ver3_member_mvc.dto.MemberResponseDto;
import com.example.ver3_member_mvc.exception.DuplicateEmailException;
import com.example.ver3_member_mvc.exception.MemberNotFoundException;
import com.example.ver3_member_mvc.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void getMemberList() throws Exception {

        // given
        List<MemberResponseDto> members = List.of(
                new MemberResponseDto(1L, "Kim", "kim@test.com"),
                new MemberResponseDto(2L, "Lee", "lee@test.com")
        );

        given(memberService.findAll()).willReturn(members);

        // when
        var result = mockMvc.perform(get("/members"));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attributeExists("members"))
                .andExpect(content().string(containsString("Kim")));
    }

    @Test
    void getNewMemberForm() throws Exception {

        // given
        // 별도 준비 없음

        // when
        var result = mockMvc.perform(get("/members/new"));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().attributeExists("memberForm"));
    }

    @Test
    void createMember() throws Exception {

        // given
        given(memberService.create(any(MemberRequestDto.class)))
                .willReturn(1L);

        // when
        var result = mockMvc.perform(post("/members")
                .param("name", "Kim")
                .param("email", "kim@test.com"));

        // then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"));

        verify(memberService).create(argThat(request ->
                request.getName().equals("Kim")
                        && request.getEmail().equals("kim@test.com")
        ));
    }

    @Test
    void createMemberWithInvalidInput() throws Exception {

        // given
        // 잘못된 입력값 준비

        // when
        var result = mockMvc.perform(post("/members")
                .param("name", "")
                .param("email", "invalid-email"));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().attributeHasFieldErrors(
                        "memberForm", "name", "email"
                ));

        verifyNoInteractions(memberService);
    }

    @Test
    void createMemberWithDuplicateEmail() throws Exception {

        // given
        given(memberService.create(any(MemberRequestDto.class)))
                .willThrow(new DuplicateEmailException());

        // when
        var result = mockMvc.perform(post("/members")
                .param("name", "Kim")
                .param("email", "kim@test.com"));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().attributeHasFieldErrors(
                        "memberForm", "email"
                ));
    }

    @Test
    void getMemberDetail() throws Exception {

        // given
        MemberResponseDto member =
                new MemberResponseDto(1L, "Kim", "kim@test.com");

        given(memberService.findById(1L)).willReturn(member);

        // when
        var result = mockMvc.perform(get("/members/1"));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("members/detail"))
                .andExpect(model().attributeExists("member"))
                .andExpect(content().string(containsString("Kim")));
    }

    @Test
    void getMemberEditForm() throws Exception {

        // given
        MemberResponseDto member =
                new MemberResponseDto(1L, "Kim", "kim@test.com");

        given(memberService.findById(1L)).willReturn(member);

        // when
        var result = mockMvc.perform(get("/members/1/edit"));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("members/edit"))
                .andExpect(model().attributeExists(
                        "memberForm", "memberId"
                ));
    }

    @Test
    void updateMember() throws Exception {

        // given
        // void 메서드는 별도 스텁 없이 호출 확인

        // when
        var result = mockMvc.perform(post("/members/1/edit")
                .param("name", "Kim2")
                .param("email", "kim@test.com"));

        // then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/1"));

        verify(memberService).update(
                org.mockito.ArgumentMatchers.eq(1L),
                argThat(request ->
                        request.getName().equals("Kim2")
                                && request.getEmail().equals("kim@test.com")
                )
        );
    }

    @Test
    void deleteMember() throws Exception {

        // given
        // 별도 준비 없음

        // when
        var result = mockMvc.perform(post("/members/1/delete"));

        // then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"));

        verify(memberService).delete(1L);
    }

    @Test
    void getMemberNotFound() throws Exception {

        // given
        given(memberService.findById(999L))
                .willThrow(new MemberNotFoundException(999L));

        // when
        var result = mockMvc.perform(get("/members/999"));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(view().name("errors/404"))
                .andExpect(model().attributeExists("message"));
    }
}