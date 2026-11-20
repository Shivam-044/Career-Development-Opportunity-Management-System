package com.studenttracker.service;

import com.studenttracker.dao.CompanyDAO;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Company;
import java.util.List;

public class CompanyService {

    private final CompanyDAO companyDao;

    public CompanyService(CompanyDAO companyDao) {
        this.companyDao = companyDao;
    }

    public List<Company> search(String keyword) {
        return search(keyword, false);
    }

    public List<Company> search(String keyword, boolean includeArchived) {
        return companyDao.search(keyword, includeArchived);
    }

    public Company save(Company company) {
        if (companyDao.existsByName(company.getCompanyName(), company.getCompanyId())) {
            throw new ValidationException(company.getCompanyName()
                    + " is already saved. Edit the existing company instead of adding it again.");
        }
        if (company.getCompanyId() == null) {
            company.setCompanyId(companyDao.insert(company));
        } else {
            companyDao.update(company);
        }
        return company;
    }

    public void setArchived(Company company, boolean archived) {
        companyDao.setArchived(company.getCompanyId(), archived);
        company.setArchived(archived);
    }
}
