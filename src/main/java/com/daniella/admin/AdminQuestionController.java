package com.daniella.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.daniella.dto.UserResponseDTO;
import com.daniella.entity.Question;
import com.daniella.enums.Difficulty;
import com.daniella.enums.QuestionStatus;
import com.daniella.repository.QuestionRepository;
import com.daniella.service.AdminAuditService;
import com.daniella.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin/questions")
public class AdminQuestionController {

    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final AdminAuditService adminAuditService;
    private final ObjectMapper objectMapper;

    public AdminQuestionController(QuestionRepository questionRepository,
                                   UserService userService,
                                   AdminAuditService adminAuditService,
                                   ObjectMapper objectMapper) {
        this.questionRepository = questionRepository;
        this.userService = userService;
        this.adminAuditService = adminAuditService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String listQuestions(@RequestParam(defaultValue = "") String search,
                                @RequestParam(required = false) String difficulty,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String status,
                                Model model,
                                Principal principal) {
        addAdminContext(model, principal);
        List<Question> allQuestions = questionRepository.findAll().stream()
                .sorted(Comparator.comparing(Question::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(question -> normalizedSearch.isBlank()
                        || question.getQuestionText().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .filter(question -> difficulty == null || difficulty.isBlank()
                        || (question.getDifficulty() != null && question.getDifficulty().name().equalsIgnoreCase(difficulty)))
                .filter(question -> category == null || category.isBlank()
                        || (question.getCategory() != null && question.getCategory().equalsIgnoreCase(category)))
                .filter(question -> status == null || status.isBlank()
                        || (question.getStatus() != null && question.getStatus().name().equalsIgnoreCase(status)))
                .collect(Collectors.toList());

        model.addAttribute("questions", filteredQuestions);
        model.addAttribute("search", search);
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("categories", allQuestions.stream()
                .map(Question::getCategory)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList()));
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("statuses", QuestionStatus.values());
        model.addAttribute("totalQuestions", allQuestions.size());
        model.addAttribute("activeQuestions", allQuestions.stream().filter(question -> question.getStatus() == QuestionStatus.ACTIVE).count());
        model.addAttribute("draftQuestions", allQuestions.stream().filter(question -> question.getStatus() == QuestionStatus.DRAFT).count());
        model.addAttribute("duplicateCount", countDuplicateQuestions(allQuestions));
        return "admin/questions";
    }

    @GetMapping("/new")
    public String newQuestion(Model model, Principal principal) {
        addAdminContext(model, principal);
        model.addAttribute("question", new Question());
        model.addAttribute("formAction", "/admin/questions/new");
        model.addAttribute("pageTitle", "Add New Question");
        model.addAttribute("submitLabel", "Save Question");
        model.addAttribute("statuses", QuestionStatus.values());
        return "admin/new-question";
    }

    @PostMapping("/new")
    public String saveQuestion(@ModelAttribute Question question, Principal principal, RedirectAttributes redirectAttributes) {
        if (question.getStatus() == null) {
            question.setStatus(QuestionStatus.ACTIVE);
        }
        questionRepository.save(question);
        adminAuditService.log(principal, "CREATE_QUESTION", "QUESTION", String.valueOf(question.getId()), question.getQuestionText());
        redirectAttributes.addFlashAttribute("success", "Question saved successfully.");
        return "redirect:/admin/questions";
    }

    @GetMapping("/{id}/edit")
    public String editQuestion(@PathVariable Long id, Model model, Principal principal) {
        addAdminContext(model, principal);
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        model.addAttribute("question", question);
        model.addAttribute("formAction", "/admin/questions/" + id + "/edit");
        model.addAttribute("pageTitle", "Edit Question");
        model.addAttribute("submitLabel", "Update Question");
        model.addAttribute("statuses", QuestionStatus.values());
        return "admin/new-question";
    }

    @PostMapping("/{id}/edit")
    public String updateQuestion(@PathVariable Long id,
                                 @ModelAttribute Question question,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        Question existing = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        existing.setQuestionText(question.getQuestionText());
        existing.setOptionA(question.getOptionA());
        existing.setOptionB(question.getOptionB());
        existing.setOptionC(question.getOptionC());
        existing.setOptionD(question.getOptionD());
        existing.setCorrectOption(question.getCorrectOption());
        existing.setDifficulty(question.getDifficulty());
        existing.setCategory(question.getCategory());
        existing.setStatus(question.getStatus());
        questionRepository.save(existing);
        adminAuditService.log(principal, "UPDATE_QUESTION", "QUESTION", String.valueOf(id), existing.getQuestionText());
        redirectAttributes.addFlashAttribute("success", "Question updated successfully.");
        return "redirect:/admin/questions";
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        questionRepository.deleteById(id);
        adminAuditService.log(principal, "DELETE_QUESTION", "QUESTION", String.valueOf(id), "Question deleted");
        redirectAttributes.addFlashAttribute("success", "Question deleted successfully.");
        return "redirect:/admin/questions";
    }

    @PostMapping("/import")
    public String importQuestions(@RequestParam("file") MultipartFile file,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("success", "Choose a JSON file to import.");
            return "redirect:/admin/questions";
        }

        try {
            Question[] imported = objectMapper.readValue(file.getInputStream(), Question[].class);
            questionRepository.saveAll(List.of(imported));
            adminAuditService.log(principal, "IMPORT_QUESTIONS", "QUESTION", null, imported.length + " question(s) imported");
            redirectAttributes.addFlashAttribute("success", imported.length + " question(s) imported successfully.");
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("success", "Import failed. Please upload a valid JSON array.");
        }
        return "redirect:/admin/questions";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportQuestions() throws IOException {
        byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(questionRepository.findAll())
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=javajolt-questions.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBytes);
    }

    private long countDuplicateQuestions(List<Question> questions) {
        return questions.stream()
                .collect(Collectors.groupingBy(question -> question.getQuestionText().trim().toLowerCase(Locale.ROOT), Collectors.counting()))
                .values()
                .stream()
                .filter(count -> count > 1)
                .count();
    }

    private void addAdminContext(Model model, Principal principal) {
        UserResponseDTO currentAdmin = userService.getByEmail(principal.getName());
        model.addAttribute("activePage", "questions");
        model.addAttribute("firstName", currentAdmin.firstName());
        model.addAttribute("lastName", currentAdmin.lastName());
        model.addAttribute("email", currentAdmin.email());
    }
}
