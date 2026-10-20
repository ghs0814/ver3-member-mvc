package com.example.ver3_member_mvc.controller;

import com.example.ver3_member_mvc.dto.MemberRequestDto;
import com.example.ver3_member_mvc.dto.MemberResponseDto;
import com.example.ver3_member_mvc.exception.DuplicateEmailException;
import com.example.ver3_member_mvc.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 회원 목록 화면
    @GetMapping
    public String list(Model model) {

        //List<MemaberResponseDto>
        model.addAttribute("members", memberService.findAll());


        return "members/list";
    } // 모델 박스에 내용물을 채워줄테니, list 페이지로 가라

    // 회원 등록 화면
    @GetMapping("/new")
    public String newForm(Model model) {

        model.addAttribute("memberForm", new MemberRequestDto());

        return "members/form";
    } // 모델 박스(dto)를 갖고, 신청 form으로 가서 박스를 채워라

    // 회원 등록 처리
    @PostMapping
    public String create(
            @Valid @ModelAttribute("memberForm") MemberRequestDto request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "members/form";
        }

        try {
            memberService.create(request);
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage()); // 개발자가 수동으로 오류 추가
            return "members/form";
        }

        return "redirect:/members";
    } // P R G 패턴

    // 회원 상세 조회
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("member", memberService.findById(id));

        return "members/detail";
    }

    // 회원 수정 화면
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        MemberResponseDto member = memberService.findById(id);

        MemberRequestDto memberForm = new MemberRequestDto();
        memberForm.setName(member.getName());
        memberForm.setEmail(member.getEmail());

        model.addAttribute("memberForm", memberForm);
        model.addAttribute("memberId", id);

        return "members/edit";
    }

    // 회원 수정 처리
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("memberForm") MemberRequestDto request, //자동으로 모델에 바인딩한 객체를 등록
            BindingResult bindingResult, // 자동으로 모델에 등록
            Model model) { // 모델에 추가적으로 작업할게 있으니 따로 선언

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", id);
            return "members/edit";
        }

        try {
            memberService.update(id, request);
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            model.addAttribute("memberId", id);
            return "members/edit";
        }

        return "redirect:/members/" + id;
    }

    // 회원 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {

        memberService.delete(id);

        return "redirect:/members";
    }
}