package com.studenttracker.service;

import com.studenttracker.dao.CompanyDAO;
import com.studenttracker.model.Company;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class InMemoryCompanyDao implements CompanyDAO {

    private final Map<Long, Company> companies = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public long insert(Company company) {
        long id = nextId++;
        company.setCompanyId(id);
        companies.put(id, company);
        return id;
    }

    @Override
    public void update(Company company) {
        companies.put(company.getCompanyId(), company);
    }

    @Override
    public Optional<Company> findById(long id) {
        return Optional.ofNullable(companies.get(id));
    }

    @Override
    public List<Company> search(String keyword, boolean includeArchived) {
        return companies.values().stream()
                .filter(company -> includeArchived || !company.isArchived())
                .filter(company -> company.getCompanyName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        return companies.values().stream().anyMatch(company ->
                company.getCompanyName().equalsIgnoreCase(name) && !company.getCompanyId().equals(excludeId));
    }

    @Override
    public void setArchived(long id, boolean archived) {
        companies.get(id).setArchived(archived);
    }
}
