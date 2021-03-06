package ru.naumen.ectmapi.security.permissions.evaluators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.naumen.ectmapi.repository.CommentRepository;
import ru.naumen.ectmapi.security.permissions.constants.Domains;
import ru.naumen.ectmapi.security.permissions.constants.Permissions;

import java.util.Map;
import java.util.function.BiFunction;

@Component(Domains.COMMENT + MainPermissionEvaluator.PERMISSION_EVALUATOR)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CommentPermissionEvaluator extends DomainPermissionEvaluatorBase {

    private final CommentRepository commentRepository;

    private final Map<String, BiFunction<Authentication, Long, Boolean>> permissionCheckers = Map.of(
            Permissions.DELETE, this::userIsAuthor
    );

    @Override
    protected Long getAuthorId(Long entityId) {
        return commentRepository.find(entityId).getAuthorId();
    }

    @Override
    protected Map<String, BiFunction<Authentication, Long, Boolean>> getPermissionCheckers() {
        return permissionCheckers;
    }
}
