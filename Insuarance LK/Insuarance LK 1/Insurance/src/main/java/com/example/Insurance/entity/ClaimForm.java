// src/main/java/com/example/Insurance/entity/ClaimForm.java
package com.example.Insurance.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claim_forms")
public class ClaimForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String formName;

    @Column(columnDefinition = "TEXT")
    private String instructions;



    @OneToMany(mappedBy = "claimForm", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    private List<FormField> formFields = new ArrayList<>();

    // Constructors
    public ClaimForm() {}

    public ClaimForm(String formName, String instructions) {
        this.formName = formName;
        this.instructions = instructions;
    }

    // Helper method to properly set relationship
    public void addFormField(FormField field) {
        formFields.add(field);
        field.setClaimForm(this);
    }

    public void removeFormField(FormField field) {
        formFields.remove(field);
        field.setClaimForm(null);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }



    public List<FormField> getFormFields() { return formFields; }

    public void setFormFields(List<FormField> formFields) {
        this.formFields.clear();
        if (formFields != null) {
            for (FormField field : formFields) {
                this.addFormField(field);
            }
        }
    }
}
