package com.example.ver3_member_mvc.service;

import com.example.ver3_member_mvc.dto.MemberRequestDto;
import com.example.ver3_member_mvc.dto.MemberResponseDto;
import com.example.ver3_member_mvc.entity.Member;
import com.example.ver3_member_mvc.exception.DuplicateEmailException;
import com.example.ver3_member_mvc.exception.MemberNotFoundException;
import com.example.ver3_member_mvc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원 등록
    @Transactional //readonly = false 덮어쓰기
    public Long create(MemberRequestDto request) {

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        return memberRepository.save(member).getId();
    }

    // 전체 회원 조회
    public List<MemberResponseDto> findAll() {

        return memberRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "id")
                ).stream()
                .map(MemberResponseDto::from)
                .toList();
    }

    // 회원 상세 조회
    public MemberResponseDto findById(Long id) {

        Member member = getMember(id);

        return MemberResponseDto.from(member);
    }

    // 회원 수정
    @Transactional
    public void update(Long id, MemberRequestDto request) {

        Member member = getMember(id);


        // 1.원래 이메일이랑 다르면서, 이미 쓰고있으면 중복처리
        // 2.원래 이메일이랑 같으면서, 이미 쓰고있으면 패스 -> 이메일 변화없음
        // -> 원래 쓰고있떤거넫, 본인거면 패스, 본인거아니면 중복 -> 본인거면 !true = false => 에러발생안함
        if (!member.getEmail().equals(request.getEmail())
                && memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        member.update(
                request.getName(),
                request.getEmail()
        );
    }

    // 회원 삭제
    @Transactional
    public void delete(Long id) {

        Member member = getMember(id);

        memberRepository.delete(member);
    }

    // 내부 공통 조회 메서드
    private Member getMember(Long id) {

        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new MemberNotFoundException(id)
                );
    }
}