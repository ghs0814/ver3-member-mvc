package com.example.ver3_member_mvc.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("회원을 찾을 수 없습니다. id=" + id);
    }
}