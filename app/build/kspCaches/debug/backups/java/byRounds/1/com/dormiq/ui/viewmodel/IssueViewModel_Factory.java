package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.IssueRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class IssueViewModel_Factory implements Factory<IssueViewModel> {
  private final Provider<IssueRepository> repositoryProvider;

  public IssueViewModel_Factory(Provider<IssueRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public IssueViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static IssueViewModel_Factory create(Provider<IssueRepository> repositoryProvider) {
    return new IssueViewModel_Factory(repositoryProvider);
  }

  public static IssueViewModel newInstance(IssueRepository repository) {
    return new IssueViewModel(repository);
  }
}
