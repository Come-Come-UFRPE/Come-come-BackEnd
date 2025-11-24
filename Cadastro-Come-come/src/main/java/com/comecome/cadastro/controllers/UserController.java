package com.comecome.cadastro.controllers;

import com.comecome.cadastro.dtos.UserPatchRecordDto;
import com.comecome.cadastro.dtos.UserRecordDto;
import com.comecome.cadastro.dtos.UserResponseDTO;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> modifyUser(@PathVariable("id") UUID id, @RequestBody UserPatchRecordDto patchRecordDto){
        User userSalvo = userService.partialUpdate(id, patchRecordDto);
        UserResponseDTO userResponseDTO = new UserResponseDTO(userSalvo);

        return ResponseEntity.ok(userResponseDTO);
    }

    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody @Valid UserRecordDto userRecordDto){
        var user = new User();
        BeanUtils.copyProperties(userRecordDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeUser (@PathVariable("id") UUID id){
        userService.removeUser(id);
        return ResponseEntity.ok("Usuário removido com sucesso!");
    }
    
}
