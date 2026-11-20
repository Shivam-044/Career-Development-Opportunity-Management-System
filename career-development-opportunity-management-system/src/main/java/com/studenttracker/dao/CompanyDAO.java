package com.studenttracker.dao;

import com.studenttracker.model.Company;
import java.util.List;
import java.util.Optional;

public interface CompanyDAO {

    long insert(Company company);

    void update(Company company);

    Optional<Company> findById(long id);

    List<Company> search(String keyword, boolean includeArchived);

    /** True if another company (not {@code excludeId}) already uses the name. */
    boolean existsByName(String name, Long excludeId);

    void setArchived(long id, boolean archived);
}
