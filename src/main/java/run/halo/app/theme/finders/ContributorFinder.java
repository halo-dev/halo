package run.halo.app.theme.finders;

import java.util.List;
import run.halo.app.core.extension.User;

/**
 * A finder for {@link User}.
 */
public interface ContributorFinder {

    Contributor getContributor(String name);

    List<Contributor> getContributors(List<String> names);
}
