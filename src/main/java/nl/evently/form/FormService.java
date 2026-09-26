package nl.evently.form;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FormService {

    private final FormRepository forms;

    public FormService(FormRepository forms) {
        this.forms = forms;
    }

    @Transactional
    public FormResponse create(CreateFormRequest request) {
        Form form = forms.save(new Form(request.name(), request.fields()));
        return FormResponse.from(form);
    }

    public List<FormResponse> findAll() {
        return forms.findAll().stream().map(FormResponse::from).toList();
    }

    public FormResponse get(UUID id) {
        return forms.findById(id).map(FormResponse::from).orElseThrow(() -> new FormNotFoundException(id));
    }
}
