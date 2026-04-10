package com.stellar.crm.inquiryservice.repository;

import com.stellar.crm.inquiryservice.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InquiryRepository
        extends JpaRepository<Inquiry, UUID>, JpaSpecificationExecutor<Inquiry> {
}

