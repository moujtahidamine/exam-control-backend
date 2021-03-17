package org.d3h.application.controllers;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.d3h.application.models.Exam;
import org.d3h.application.models.Professor;
import org.d3h.application.models.Session;
import org.d3h.application.models.Student;
import org.d3h.application.payload.ApiResponse;
import org.d3h.application.services.ExamService;
import org.d3h.application.services.ModuleService;
import org.d3h.application.services.ProfessorService;
import org.d3h.application.services.RoomService;
import org.d3h.application.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"*"})
public class ExamController {

	@Autowired
	private ExamService examService;
	
	@Autowired
	private ProfessorService profService;
	
	@Autowired
	private RoomService roomService;
	
	@Autowired
	private ModuleService moduleService;
	
	@Autowired
	private StudentService studentService;
	
	//---GET---
	@GetMapping(value="/exams")
	public List<Exam> getAllExams(){
		
		return(examService.getAll());		
	}
	
	@GetMapping(value="/exams/{id}")
	public Exam getExamById(@PathVariable long id){
		
		return examService.getById(id);		
	}
	
	@GetMapping(value="/exams/{id}/participants")
	public List<Student> getListParticipants(@PathVariable long id){
		
		return examService.getListParticiants(id);		
	}
	
	@GetMapping(value="/exams/{id}/surveillants")
	public List<Professor> getListSurveillants(@PathVariable long id){
		
		return examService.getListSurveillants(id);		
	}
	
	//---------
	
	@GetMapping(value="/exams/students/{idStudent}")
	public List<Exam> getExamsByStudents(@PathVariable long idStudent){
		
		return examService.getExamsByStudent(idStudent);		
	}
	
	@GetMapping(value="/exams/surveillants/{idSurveillant}")
	public List<Exam> getExamsBySurveillants(@PathVariable long idSurveillant){
		
		return examService.getExamsBySurveillants(idSurveillant);		
	}
	
	//POST
	//The final version would be : PostMapping 
	@GetMapping("/exams/test")
	public ResponseEntity<?> createExam(/*@RequestBody Exam exam*/) {
		/*
		 * body request :	
		 * 		module + room + datetime + list surveillants + session 
		*/
		LocalDateTime d = LocalDateTime.of(2015,Month.JULY, 29, 10, 30, 00);
				
		Exam savedExam = examService.create(moduleService.getById(1), roomService.getById(1), d, profService.getAll(), Session.NORMALE);
		
		
		//Exam savedExam = examService.create(exam.getModule(), exam.getRoom(), exam.getDate(), exam.getSurveillants(), exam.getSession());
		if(savedExam == null)
			return new ResponseEntity<>(new ApiResponse(false,"This exam is already existed!"),HttpStatus.BAD_REQUEST);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(savedExam.getId()).toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true,"This Exam is sucessfully created"));
	}
	

	
	//PUT
	@PutMapping("/exams/{id}")
	public ResponseEntity<?> updateExam(@RequestBody Exam exam,@PathVariable long id) {
		
		Exam updatedExam = examService.update(exam, id);
		if(updatedExam == null)
			return new ResponseEntity<>(new ApiResponse(false, "cet ID ne correspond à aucun éxamen !"), HttpStatus.BAD_REQUEST);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
		.buildAndExpand(updatedExam.getId()).toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true,"This Exam is sucessfully updated"));
	}
	
	
	@GetMapping(value="/exams/scan/{idExam}/{apogee}")
	public boolean setStudentPresence(@PathVariable String apogee, @PathVariable long idExam){
		
		boolean p = true;
		Student s = studentService.getByApogee(apogee);
		//Il faut Traiter le cas ou l'étudiant n'a pas le droit de participer à l'examen 
		return examService.setStudentPresence(idExam, s.getId(), p);	
	}
	
	//DELETE
	@DeleteMapping("/exams/{id}")
	public ResponseEntity<?> deleteExam(@PathVariable long id) {
		boolean isDeleted = examService.deleteById(id);
		if(!isDeleted)
			return new ResponseEntity<>(new ApiResponse(false, "cet ID ne correspond à aucun éxamen !"), HttpStatus.BAD_REQUEST);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
		.buildAndExpand(deleteExam(id)).toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true,"Exam is sucessfully deleted"));
	}
	
	//---EXPORT as PDF---
	@GetMapping(value="/exams/export/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<InputStreamResource> exportExamList(@PathVariable long id) {

		ByteArrayInputStream out = examService.exportExamList(id);
		
		String fileName = "Liste_Examen_"+examService.getById(id).getId();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline;"+ "filename="+fileName+".pdf");

        return ResponseEntity
            .ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(out));
		
	}
}