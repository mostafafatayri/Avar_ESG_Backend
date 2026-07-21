package com.fatayriTech.avarESG.service.SecurityService;



//import com.fatayriTech.avarLMS.repository.Employee.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentEmployeeService {

  /*  private final EmployeeRepo employeeRepo;

    public Employee getCurrentEmployee(Long organizationId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return employeeRepo
                .findByMasterUserIdAndOrganizationId(currentUserId, organizationId)
                .orElseThrow(() -> new RuntimeException("Current user is not linked to an employee in this organization"));
    }*/
}