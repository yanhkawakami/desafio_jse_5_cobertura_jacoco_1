package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

    @Mock
    ScoreRepository repository;

    @Mock
    MovieRepository movieRepository;

    @Mock
    UserService userService;

    private ScoreDTO scoreDTO;
    private ScoreDTO scoreDTONonExistingMovie;
    private UserEntity user;
    private MovieEntity movie;
    private MovieEntity nonExistingMovie;
    private ScoreEntity score;
    private ScoreEntity scoreNonExistingMovie;
    private Double scoreAvg;

    private Long nonExistingMovieId;

    @BeforeEach
    void setUp(){
        score = ScoreFactory.createScoreEntity();
        scoreDTO = ScoreFactory.createScoreDTO();

        movie = MovieFactory.createMovieEntity();
        movie.getScores().add(score);
        nonExistingMovie = MovieFactory.createMovieEntity();
        nonExistingMovie.setId(nonExistingMovieId);

        scoreNonExistingMovie = ScoreFactory.createScoreEntity();
        scoreNonExistingMovie.setMovie(nonExistingMovie);
        scoreDTONonExistingMovie = new ScoreDTO(scoreNonExistingMovie);

        user = UserFactory.createUserEntity();

        nonExistingMovieId = 2L;

        Mockito.when(userService.authenticated()).thenReturn(user);

        Mockito.when(movieRepository.findById(movie.getId())).thenReturn(Optional.ofNullable(movie));
        Mockito.when(movieRepository.findById(nonExistingMovieId)).thenThrow(ResourceNotFoundException.class);

        Mockito.when(repository.saveAndFlush(score)).thenReturn(score);

        double sum = 0.0;
        for (ScoreEntity s : movie.getScores()){
            sum = sum + s.getValue();
        }
        scoreAvg = sum / movie.getScores().size();
        movie.setScore(scoreAvg);
        movie.setCount(movie.getScores().size());

        Mockito.when(movieRepository.save(movie)).thenReturn(movie);
    }

	@Test
	public void saveScoreShouldReturnMovieDTO() {

        MovieDTO result = service.saveScore(scoreDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(movie.getScores().size(), result.getCount());
        Assertions.assertEquals(scoreAvg, result.getScore());

	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.saveScore(scoreDTONonExistingMovie);
        });
	}
}
