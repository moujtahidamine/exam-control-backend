package org.d3h.application.repositories;

import org.d3h.application.models.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long>{

	@Query("SELECT p FROM Professor p WHERE p.contact.email = ?1 and p.cin = ?2")
	Professor getByEmailCin(String email, String cin);

}
