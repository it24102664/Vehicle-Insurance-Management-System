// src/main/java/com/example/Insurance/service/ClaimFormService.java
package com.example.Insurance.service;

import com.example.Insurance.entity.ClaimForm;
import com.example.Insurance.entity.FormField;
import com.example.Insurance.repository.ClaimFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ClaimFormService {

    @Autowired
    private ClaimFormRepository claimFormRepository;

    public List<ClaimForm> getAllForms() {
        return claimFormRepository.findAll();
    }

    public ClaimForm getFormById(Long id) {
        return claimFormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));
    }

    public ClaimForm getFormByName(String formName) {
        return claimFormRepository.findByFormName(formName)
                .orElseThrow(() -> new RuntimeException("Form not found with name: " + formName));
    }

    @Transactional
    public ClaimForm createForm(ClaimForm claimForm) {
        // Check if form name already exists
        if (claimFormRepository.existsByFormName(claimForm.getFormName())) {
            throw new RuntimeException("Form with name '" + claimForm.getFormName() + "' already exists");
        }

        // Set timestamps


        // First save the form without fields
        ClaimForm savedForm = claimFormRepository.save(claimForm);

        // Then handle the fields
        if (claimForm.getFormFields() != null && !claimForm.getFormFields().isEmpty()) {
            // Clear existing fields and add new ones with proper relationship
            savedForm.getFormFields().clear();

            for (FormField field : claimForm.getFormFields()) {
                // Create new field and set the relationship properly
                FormField newField = new FormField();
                newField.setFieldLabel(field.getFieldLabel());
                newField.setFieldType(field.getFieldType());
                newField.setIsRequired(field.getIsRequired());
                newField.setFieldOrder(field.getFieldOrder());
                newField.setClaimForm(savedForm); // This is crucial

                savedForm.getFormFields().add(newField);
            }

            // Save again with fields
            savedForm = claimFormRepository.save(savedForm);
        }

        return savedForm;
    }

    @Transactional
    public ClaimForm updateForm(String formName, ClaimForm updatedForm) {
        ClaimForm existingForm = getFormByName(formName);

        // Update basic properties
        existingForm.setFormName(updatedForm.getFormName());
        existingForm.setInstructions(updatedForm.getInstructions());


        // Clear existing fields
        existingForm.getFormFields().clear();

        // Add new fields with proper relationship
        if (updatedForm.getFormFields() != null) {
            for (FormField field : updatedForm.getFormFields()) {
                FormField newField = new FormField();
                newField.setFieldLabel(field.getFieldLabel());
                newField.setFieldType(field.getFieldType());
                newField.setIsRequired(field.getIsRequired());
                newField.setFieldOrder(field.getFieldOrder());
                newField.setClaimForm(existingForm); // This is crucial

                existingForm.getFormFields().add(newField);
            }
        }

        return claimFormRepository.save(existingForm);
    }

    @Transactional
    public void deleteForm(String formName) {
        ClaimForm form = getFormByName(formName);
        claimFormRepository.delete(form);
    }


    public boolean isFormAvailable(Long formId) {

        return false;
    }
}
