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
public final class SupabaseBedRepository_Factory implements Factory<SupabaseBedRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabaseBedRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabaseBedRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabaseBedRepository_Factory create(Provider<SupabaseClient> supabaseProvider) {
    return new SupabaseBedRepository_Factory(supabaseProvider);
  }

  public static SupabaseBedRepository newInstance(SupabaseClient supabase) {
    return new SupabaseBedRepository(supabase);
  }
}
