package com.heejong.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.heejong.hr.entity.Member;

@Mapper
public interface LoginMapper {

    /**
     * 이메일로 사용자 조회
     *
     * @param email 사용자 식별자
     * @return 조회된 Member (없으면 null)
     */
    Member findByEmail(String email);

    /**
     * ID로 사용자 조회
     *
     * @param id 문자열 ID
     * @return 조회된 Member (없으면 null)
     */
    Member findById(String id);

    /**
     * 회원번호로 사용자 조회
     *
     * @param memberNo 회원번호
     * @return 조회된 Member (없으면 null)
     */
    Member findByMemberNo(Long memberNo);

    /**
     * 로그인 인증 처리
     *
     * @param email 사용자 식별자
     * @param rawPassword 평문 비밀번호
     * @return 인증된 Member
     */
    Member authenticate(String email, String rawPassword);

    /**
     * 회원 가입
     *
     * @param member 가입할 회원 정보
     * @return 삽입된 행 수
     */
    int insertMember(Member member);

    /**
     * 회원 탈퇴
     *
     * @param email 탈퇴할 회원의 이메일
     * @return 삭제된 행 수
     */
    int deleteMember(String email);

    /**
     * 모든 회원 조회 (관리자용)
     *
     * @return 모든 회원 목록
     */
    List<Member> findAll();

}
