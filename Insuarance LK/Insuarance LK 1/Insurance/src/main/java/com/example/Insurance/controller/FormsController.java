// src/main/java/com/example/Insurance/controller/FormsController.java
package com.example.Insurance.controller;

import com.example.Insurance.entity.ClaimForm;
import com.example.Insurance.service.ClaimFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/forms")
@CrossOrigin(origins = "*")
public class FormsController {

    @Autowired
    private ClaimFormService claimFormService;

    @GetMapping
    public ResponseEntity<List<ClaimForm>> getAllForms() {
        try {
            List<ClaimForm> forms = claimFormService.getAllForms();
            System.out.println("Retrieved " + forms.size() + " forms");
            return ResponseEntity.ok(forms);
        } catch (Exception e) {
            System.err.println("Error getting all forms: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimForm> getFormById(@PathVariable Long id) {
        try {
            ClaimForm form = claimFormService.getFormById(id);
            return ResponseEntity.ok(form);
        } catch (Exception e) {
            System.err.println("Error getting form by id: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createForm(@RequestBody ClaimForm claimForm) {
        try {
            System.out.println("Creating form: " + claimForm.getFormName());
            System.out.println("Form fields count: " + (claimForm.getFormFields() != null ? claimForm.getFormFields().size() : 0));

            ClaimForm createdForm = claimFormService.createForm(claimForm);
            System.out.println("Form created successfully with ID: " + createdForm.getId());

            return ResponseEntity.ok(createdForm);
        } catch (RuntimeException e) {
            System.err.println("Business error creating form: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Technical error creating form: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{formName}")
    public ResponseEntity<?> updateForm(@PathVariable String formName, @RequestBody ClaimForm claimForm) {
        try {
            System.out.println("Updating form: " + formName);
            ClaimForm updatedForm = claimFormService.updateForm(formName, claimForm);
            return ResponseEntity.ok(updatedForm);
        } catch (RuntimeException e) {
            System.err.println("Business error updating form: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Technical error updating form: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/name/{formName}")
    public ResponseEntity<?> deleteForm(@PathVariable String formName) {
        try {
            System.out.println("Deleting form: " + formName);
            claimFormService.deleteForm(formName);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("Business error deleting form: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Technical error deleting form: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }
}
