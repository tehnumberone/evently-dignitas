package nl.evently.form;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }

    @PostMapping
    public ResponseEntity<FormResponse> create(@Valid @RequestBody CreateFormRequest request) {
        FormResponse form = formService.create(request);
        return ResponseEntity.created(URI.create("/api/forms/" + form.id())).body(form);
    }

    @GetMapping
    public List<FormResponse> findAll() {
        return formService.findAll();
    }

    @GetMapping("/{id}")
    public FormResponse get(@PathVariable UUID id) {
        return formService.get(id);
    }
}
