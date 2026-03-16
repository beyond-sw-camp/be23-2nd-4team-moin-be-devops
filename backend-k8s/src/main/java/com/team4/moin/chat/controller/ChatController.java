package com.team4.moin.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team4.moin.chat.dtos.*;
import com.team4.moin.chat.service.ChatMessageService;
import com.team4.moin.chat.service.ChatService;
import com.team4.moin.crew.domain.enums.CrewRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageService chatMessageService;

    @Autowired
    public ChatController(ChatService chatService, ChatMessageService chatMessageService) {
        this.chatService = chatService;
        this.chatMessageService = chatMessageService;
    }

    //    그룹채팅방참여 : 크루 승인 시, 그룹채팅방에 추가
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok().build();
    }

    //    개인 채팅방 개설 또는 기존 roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestBody PrivateRoomCreateDto dto) {
        PrivateRoomResDto result = chatService.getOrCreatePrivateRoom(dto.getOtherMemberId(), dto.getCrewId());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //    1:1 채팅방 나가기
    @DeleteMapping("/room/private/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId){
        chatService.softLeaveChatRoom(roomId);
        return ResponseEntity.ok().build();
    }

//    Presigned URL 발급
    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String fileName) {
        String url = chatMessageService.getPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/imageSend")
    public ResponseEntity<?> sendImageMessage(@RequestBody ChatImageDto dto) {
        chatMessageService.saveChatImages(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/message/{messageId}/delete")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        chatMessageService.deleteMessage(messageId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/message/update")
    public ResponseEntity<?> updateMessage(@RequestBody MessageUpdateDto dto) {
        chatMessageService.updateMessage(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room/{roomId}/my-role")
    public ResponseEntity<?> getMyRole(@PathVariable Long roomId) {
        CrewRole role = chatService.getMyRoleInRoom(roomId);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    //    0) 내 전체 채팅방 목록 조회
    @GetMapping("/my/all")
    public ResponseEntity<?> getAllMyChatRooms(@ModelAttribute ChatRoomSearchDto searchDto) {
        List<MyChatListResDto> result = chatService.getAllMyChatRooms(searchDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //    1) 내 그룹채팅목록 조회
    @GetMapping("/my/groupRooms")
    public ResponseEntity<?> getMyGroupChatRooms(@ModelAttribute ChatRoomSearchDto searchDto) {
        List<MyChatListResDto> result = chatService.getMyGroupChatRooms(searchDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //    2)내 1:1 채팅방 목록 조회 : roomId, roomName, 그룹채팅여부, 메시지 읽음 개수
    @GetMapping("/my/privateRooms")
    public ResponseEntity<?> getMyChatRooms(@ModelAttribute ChatRoomSearchDto searchDto) {
        List<MyChatListResDto> result = chatService.getMyChatRooms(searchDto);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    //    3)해당 크루 채팅방 조회
    @GetMapping("/crew/chatRoom/{crewId}")
    public ResponseEntity<?> getCrewChatRoom(@PathVariable Long crewId, @ModelAttribute ChatRoomSearchDto searchDto) {
        List<MyChatListResDto> result = chatService.getCrewChatRoom(crewId, searchDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    //    이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> chatMessageDtos = chatMessageService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    //    채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId) {
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

}

