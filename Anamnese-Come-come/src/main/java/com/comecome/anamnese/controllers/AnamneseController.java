package com.comecome.anamnese.controllers;

import com.comecome.anamnese.dtos.AnamnesePatchRecordDTO;
import com.comecome.anamnese.dtos.AnamneseRecordDTO;
import com.comecome.anamnese.dtos.AnamneseResponseDTO;
import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.services.AnamneseService;
import jakarta.validation.Valid;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/anamnese")
public class AnamneseController {

    private final AnamneseService anamneseService;

    public AnamneseController(AnamneseService anamneseService){
        this.anamneseService = anamneseService;
    }

    @PostMapping
    public ResponseEntity<Anamnese> createAnamnese(@RequestBody @Valid AnamneseRecordDTO anamneseRecordDTO){
        var anamnese = anamneseRecordDTO.toEntity();
        System.out.println(anamnese);
        return ResponseEntity.status(HttpStatus.CREATED).body(anamneseService.save(anamnese));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnamneseResponseDTO> getAnamnese(@PathVariable("id") UUID id){
        return ResponseEntity.ok(anamneseService.getAnamneseById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AnamneseResponseDTO> modifyUser(@PathVariable("id") UUID id, @RequestBody AnamnesePatchRecordDTO patchRecordDto){
        Anamnese updateAnamnese = anamneseService.partialUpdate(id, patchRecordDto);
        var anamneseResponseDTO = new AnamneseResponseDTO(updateAnamnese);

        return ResponseEntity.ok(anamneseResponseDTO);
    }
}
