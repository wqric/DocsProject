package com.example.docsproject.common

import com.example.docsproject.data.reps_impl.PhotoRepositoryImpl
import com.example.docsproject.domain.reps.PhotoRepository
import com.example.docsproject.domain.use_cases.PhotoUseCase
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        PhotoViewModel(get())
    }

    single<PhotoUseCase> {
        PhotoUseCase(get())
    }

    single<PhotoRepository> {
        PhotoRepositoryImpl(get())
    }
}