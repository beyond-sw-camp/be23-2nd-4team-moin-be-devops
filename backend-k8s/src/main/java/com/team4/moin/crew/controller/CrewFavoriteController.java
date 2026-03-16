package com.team4.moin.crew.controller;

import com.team4.moin.crew.service.CrewFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crew")
public class CrewFavoriteController {
    private final CrewFavoriteService crewFavoriteService;
    @Autowired
    public CrewFavoriteController(CrewFavoriteService crewFavoriteService) {
        this.crewFavoriteService = crewFavoriteService;
    }
    @PostMapping("/{crewId}/favorite")
    public ResponseEntity<Boolean> addFavorite(@PathVariable Long crewId){
        crewFavoriteService.addFavorite(crewId);
        return ResponseEntity.ok(true); // 찜됨
    }

    @DeleteMapping("/{crewId}/favorite")
    public ResponseEntity<Boolean> removeFavorite(@PathVariable Long crewId){
        crewFavoriteService.removeFavorite(crewId);
        return ResponseEntity.ok(false); // 찜취소됨
    }
}
