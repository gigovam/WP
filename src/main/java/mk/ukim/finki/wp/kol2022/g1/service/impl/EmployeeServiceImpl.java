package mk.ukim.finki.wp.kol2022.g1.service.impl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidSkillIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import mk.ukim.finki.wp.kol2022.g1.service.SkillService;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, SkillRepository skillRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> listAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
    }

    @Override
    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = skillRepository.findAllById(skillId);
        String encodedPassword = passwordEncoder.encode(password);
        return employeeRepository.save(new Employee(name, email, encodedPassword, type, skills, employmentDate));
    }

    @Override
    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        Employee employee = findById(id);
        List<Skill> skills = skillRepository.findAllById(skillId);
        String encodedPassword = passwordEncoder.encode(password);

        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(encodedPassword);
        employee.setType(type);
        employee.setSkills(skills);
        employee.setEmploymentDate(employmentDate);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee delete(Long id) {
        Employee employee = findById(id);
        employeeRepository.delete(employee);
        return employee;
    }

    @Override
    public List<Employee> filter(Long skillId, Integer yearsOfService) {
        Skill skill = skillId != null ? skillRepository.findById(skillId).orElseThrow(InvalidSkillIdException::new) : null;
        if(skill!=null && yearsOfService!=null) {
            LocalDate employmentDate = LocalDate.now().minusYears((yearsOfService));
            return employeeRepository.findAllBySkillsContainingAndEmploymentDateBefore(skill, employmentDate);
        }
        else if(skill != null) {
            return employeeRepository.findAllBySkillsContaining(skill);
        }
        else if(yearsOfService != null) {
            LocalDate employmentDate = LocalDate.now().minusYears((yearsOfService));
            return employeeRepository.findAllByEmploymentDateBefore(employmentDate);
        }
        return listAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(username);

        return new User(username, employee.getPassword(),
                Collections.singleton(employee.getType()));
    }
}
