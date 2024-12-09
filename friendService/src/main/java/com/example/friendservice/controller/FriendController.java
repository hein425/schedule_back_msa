package com.example.friendservice.controller;

import com.example.friendservice.dto.response.FriendListResponseDto;
import com.example.friendservice.dto.response.UserSearchResponseDto;
import com.example.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 친구 요청 보내기
    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam(name = "requesterId") Long requesterId, @RequestParam(name = "receiverId") Long receiverId) {
        friendService.sendFriendRequest(requesterId, receiverId);
        return ResponseEntity.ok("Friend request sent successfully.");
    }

    // 친구 요청 조회
    @GetMapping("/{idx}/requests")
    public ResponseEntity<String> getFriendRequests(@PathVariable(name = "idx") Long userId) {
        ResponseEntity<List<UserSearchResponseDto>> friendRequests = friendService.getFriendRequests(userId);

        return ResponseEntity.ok("친구 요청 조회가 성공하였습니다.");
    }

    // 친구 요청 수락
    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriendRequest(@RequestParam(name = "requesterId") Long requesterId, @RequestParam(name = "receiverId") Long receiverId){
        friendService.acceptFriendRequest(requesterId, receiverId);
        return ResponseEntity.ok("Friend request accepted.");
    }

    // 친구 요청 거절
    @PostMapping("/reject")
    public ResponseEntity<String> rejectFriendRequest(@RequestParam(name = "rejectedRequestId") Long friendRequestId) {
        friendService.rejectFriendRequest(friendRequestId);
        return ResponseEntity.ok("Friend request rejected.");
    }

    // 친구 목록 조회
    @GetMapping("/{idx}/list")
    public List<UserSearchResponseDto> getFriendsList(@PathVariable(name = "idx") Long userId) {
        List<UserSearchResponseDto> friends = friendService.getFriendsList(userId);
        return friends;
    }


    // 유저 검색
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponseDto>> searchUsers(@RequestParam(name = "userId") Long userId,
                                                                   @RequestParam(name = "userName") String userName) {
        List<UserSearchResponseDto> users = friendService.searchUsersByUserName(userId, userName);
        return ResponseEntity.ok(users);
    }


    // 친구 삭제
    @PostMapping("/delete")
    public ResponseEntity<String> deleteFriend(@RequestParam(name = "userId") Long userId,
                                               @RequestParam(name = "FriendId") Long deleteFriendId) {
        friendService.deleteFriend(userId, deleteFriendId);
        return ResponseEntity.ok("Friend deleted.");
    }

}
