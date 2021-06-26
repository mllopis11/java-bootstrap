package mike.samples.webapp.module.company.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mike.samples.webapp.module.company.domain.Company;
import mike.samples.webapp.module.company.domain.CompanyFactory;
import mike.samples.webapp.module.company.exception.CompanyAlreadyExistException;
import mike.samples.webapp.module.company.exception.CompanyNotFoundException;
import mike.samples.webapp.module.company.web.model.CompanyForm;

@Service
public class CompanyService {

	private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
	
	private final ConcurrentMap<String, Company> companies = CompanyFactory.defaultCompanies();
	
	/**
	 * @return all companies
	 */
	public Collection<Company> findAll() {
		log.info("findAll: retrieve all companies ...");
		return this.companies.values();
	}
	
	/**
	 * @param name company name to retrieve
	 * @return the company if exist otherwise an empty value. 
	 */
	public Optional<Company> findByName(String name) {
		log.info("findByName: name={}", name);
		
		return companies.entrySet().stream()
				.filter( e -> e.getKey().equalsIgnoreCase(name) )
				.map(Map.Entry::getValue)
				.findFirst();
	}
	
	/**
	 * @param form company form
	 * @return the created company
	 * @throws CompanyAlreadyExistException in case of the company already exists.
	 */
	public Company create(CompanyForm form) {
		
		log.info("create: {}", form);
		
		this.findByName(form.getName())
				.ifPresent(c -> new CompanyAlreadyExistException(c.getName()));
		
		return this.saveCompany(new Company(form.getName(), form.getDescription()));
	}
	
	/**
	 * @param form company form
	 * @return the updated company
	 */
	public Company update(CompanyForm form) {
		
		log.info("update: {}", form);
		
		var company = this.findByName(form.getName())
					.map( c -> new Company(form.getName(), form.getDescription()))
					.orElseThrow(() -> new CompanyNotFoundException(form.getName()));
				
		return this.saveCompany(company);
	}
	
	/**
	 * @param name neame of the company
	 * @return true if the company has been deleted othawise false
	 */
	public boolean delete(String name) {
		log.info("delete: name={}", name);
		
		return Optional.ofNullable(this.companies.remove(name)).isPresent();
	}
	
	/**
	 * Insert or Replace the given company object
	 *  
	 * @param company the compaby to save
	 * @return the saved company
	 */
	private Company saveCompany(Company company) {
		this.companies.put(company.getName(), company);
		return company;
	}
}
