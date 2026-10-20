package com.example.ver3_member_mvc.dto;


import com.example.ver3_member_mvc.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponseDto {

    private Long id;

    private String name;

    private String email;

    //static factory method
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}