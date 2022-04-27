package run.halo.app.identity.authorization;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface AuthorizationRuleResolver {

    /**
     * rulesFor returns the list of rules that apply to a given user.
     * If an error is returned, the slice of PolicyRules may not be complete,
     * but it contains all retrievable rules.
     * This is done because policy rules are purely additive and policy determinations
     * can be made on the basis of those rules that are found.
     *
     * @param user authenticated user info
     */
    PolicyRuleList rulesFor(UserDetails user);

    /**
     * visitRulesFor invokes visitor() with each rule that applies to a given user
     * and each error encountered resolving those rules. Rule may be null if err is non-nil.
     * If visitor() returns false, visiting is short-circuited.
     *
     * @param user user info
     * @param visitor visitor
     */
    void visitRulesFor(UserDetails user, RuleAccumulator visitor);
}
