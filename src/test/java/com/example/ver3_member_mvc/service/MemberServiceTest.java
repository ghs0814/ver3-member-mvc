package com.example.ver3_member_mvc.service;

import com.example.ver3_member_mvc.dto.MemberRequestDto;
import com.example.ver3_member_mvc.dto.MemberResponseDto;
import com.example.ver3_member_mvc.entity.Member;
import com.example.ver3_member_mvc.exception.DuplicateEmailException;
import com.example.ver3_member_mvc.exception.MemberNotFoundException;
import com.example.ver3_member_mvc.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void createMember() {

        // given
        MemberRequestDto request = new MemberRequestDto();
        request.setName("Kim");
        request.setEmail("kim@test.com");

        Member savedMember = mock(Member.class);

        given(memberRepository.existsByEmail("kim@test.com"))
                .willReturn(false);

        given(memberRepository.save(any(Member.class)))
                .willReturn(savedMember);

        given(savedMember.getId()).willReturn(1L);

        // when
        Long result = memberService.create(request);

        // then
        assertEquals(1L, result);

        ArgumentCaptor<Member> captor =
                ArgumentCaptor.forClass(Member.class);

        verify(memberRepository).save(captor.capture());

        assertEquals("Kim", captor.getValue().getName());
        assertEquals("kim@test.com", captor.getValue().getEmail());
    }

    @Test
    void createMemberWithDuplicateEmail() {

        // given
        MemberRequestDto request = new MemberRequestDto();
        request.setName("Kim");
        request.setEmail("kim@test.com");

        given(memberRepository.existsByEmail("kim@test.com"))
                .willReturn(true);

        // when
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> memberService.create(request)
        );

        // then
        assertEquals("이미 사용 중인 이메일입니다.",
                exception.getMessage());

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void findMemberById() {

        // given
        Member member = Member.builder()
                .name("Kim")
                .email("kim@test.com")
                .build();

        given(memberRepository.findById(1L))
                .willReturn(Optional.of(member));

        // when
        MemberResponseDto result = memberService.findById(1L);

        // then
        assertEquals("Kim", result.getName());
        assertEquals("kim@test.com", result.getEmail());
    }

    @Test
    void findMemberByIdNotFound() {

        // given
        given(memberRepository.findById(999L))
                .willReturn(Optional.empty());

        // when
        MemberNotFoundException exception = assertThrows(
                MemberNotFoundException.class,
                () -> memberService.findById(999L)
        );

        // then
        assertEquals("회원을 찾을 수 없습니다. id=999",
                exception.getMessage());
    }

    @Test
    void updateMember() {

        // given
        Member member = Member.builder()
                .name("Kim")
                .email("kim@test.com")
                .build();

        MemberRequestDto request = new MemberRequestDto();
        request.setName("Kim2");
        request.setEmail("kim@test.com");

        given(memberRepository.findById(1L))
                .willReturn(Optional.of(member));

        // when
        memberService.update(1L, request);

        // then
        assertEquals("Kim2", member.getName());
        assertEquals("kim@test.com", member.getEmail());

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void deleteMember() {

        // given
        Member member = Member.builder()
                .name("Kim")
                .email("kim@test.com")
                .build();

        given(memberRepository.findById(1L))
                .willReturn(Optional.of(member));

        // when
        memberService.delete(1L);

        // then
        verify(memberRepository).delete(member);
    }
}