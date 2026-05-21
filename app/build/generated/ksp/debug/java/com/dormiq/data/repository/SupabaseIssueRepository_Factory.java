package com.dormiq.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SupabaseIssueRepository_Factory implements Factory<SupabaseIssueRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabaseIssueRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabaseIssueRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabaseIssueRepository_Factory create(Provider<SupabaseClient> supabaseProvider) {
    return new SupabaseIssueRepository_Factory(supabaseProvider);
  }

  public static SupabaseIssueRepository newInstance(SupabaseClient supabase) {
    return new SupabaseIssueRepository(supabase);
  }
}
