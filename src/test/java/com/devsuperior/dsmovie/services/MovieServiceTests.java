package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

    @Mock
    private MovieRepository repository;

    private MovieEntity movieEntity;
    private MovieDTO movieDTO;
    private Long existingMovieId;
    private Long nonExistingMovieId;
    private Long attachedExistingMovieId;

    private PageImpl<MovieEntity> movieEntityPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        movieEntity = MovieFactory.createMovieEntity();
        movieDTO = MovieFactory.createMovieDTO();
        existingMovieId = 1L;
        nonExistingMovieId = 1000L;
        attachedExistingMovieId = 2000L;

        movieEntityPage = new PageImpl<>(List.of(movieEntity));
        pageable = PageRequest.of(0, 12);

        Mockito.when(repository.searchByTitle(movieEntity.getTitle(), pageable)).thenReturn(movieEntityPage);
        Mockito.when(repository.findById(existingMovieId)).thenReturn(Optional.ofNullable(movieEntity));

        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(movieEntity);

        Mockito.when(repository.getReferenceById(existingMovieId)).thenReturn(movieEntity);
        Mockito.when(repository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(repository.existsById(existingMovieId)).thenReturn(Boolean.TRUE);
        Mockito.when(repository.existsById(nonExistingMovieId)).thenReturn(Boolean.FALSE);
        Mockito.when(repository.existsById(attachedExistingMovieId)).thenReturn(Boolean.TRUE);

    }


	@Test
	public void findAllShouldReturnPagedMovieDTO() {
        Page<MovieDTO> result = service.findAll(movieEntity.getTitle(), pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(movieEntity.getTitle(), result.iterator().next().getTitle());
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.findById(existingMovieId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingMovieId, result.getId());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
           service.findById(nonExistingMovieId);
        });
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
        MovieDTO result = service.insert(movieDTO);

        Assertions.assertNotNull(result);
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.update(existingMovieId, movieDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(movieDTO.getTitle(), result.getTitle());
        Assertions.assertEquals(movieDTO.getScore(), result.getScore());
        Assertions.assertEquals(movieDTO.getCount(), result.getCount());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingMovieId, movieDTO);
        });
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
        Mockito.doNothing().when(repository).deleteById(existingMovieId);

        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingMovieId);
        });
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingMovieId);
        });
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(attachedExistingMovieId);

        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(attachedExistingMovieId);
        });
	}
}
