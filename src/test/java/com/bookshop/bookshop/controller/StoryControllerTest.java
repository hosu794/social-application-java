package com.bookshop.bookshop.controller;

import com.bookshop.bookshop.model.Love;
import com.bookshop.bookshop.model.Story;
import com.bookshop.bookshop.model.Topic;
import com.bookshop.bookshop.model.User;
import com.bookshop.bookshop.payload.PagedResponse;
import com.bookshop.bookshop.payload.StoryRequest;
import com.bookshop.bookshop.payload.StoryResponse;
import com.bookshop.bookshop.repository.LoveRepository;
import com.bookshop.bookshop.repository.StoryRepository;
import com.bookshop.bookshop.repository.TopicRepository;
import com.bookshop.bookshop.repository.UserRepository;
import com.bookshop.bookshop.security.UserPrincipal;
import com.bookshop.bookshop.service.StoryService;
import com.bookshop.bookshop.service.StoryServiceImpl;
import com.bookshop.bookshop.util.AppConstants;
import org.hibernate.query.criteria.internal.expression.SimpleCaseExpression;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.core.parameters.P;

import javax.swing.text.html.Option;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StoryControllerTest {


    StoryRepository storyRepository = mock(StoryRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    LoveRepository loveRepository = mock(LoveRepository.class);
    TopicRepository topicRepository = mock(TopicRepository.class);
    StoryService storyService = new StoryServiceImpl(storyRepository, userRepository, loveRepository, topicRepository);


    @Test
    public void should_return_correct_story_by_id() throws Exception {

        Topic topic = new Topic();
        topic.setDescription("Topic Description");
        topic.setTitle("Topic title");
        topic.setId((long) 1);

        Story story = new Story();
        story.setCreatedBy((long) 1);
        story.setId((long) 1);
        story.setTopic(topic);
        story.setCreatedBy((long) 1);
        story.setTitle("Story Title");
        story.setBody("<p>Body</p>");
        story.setDescription("Story Description");

        User user = new User();
        user.setUsername("hosu794");
        user.setPassword("password");
        user.setName("Grzegorz Szczęsny");
        user.setEmail("hosu794@gmail.com");
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        when(userRepository.findById((long) 1)).thenReturn(Optional.of(user));
        when(storyRepository.findById((long) 1)).thenReturn(Optional.of(story));
        Assert.assertTrue(storyService.getStoryById( (long) 1, userPrincipal).getTitle().contains("Story Title"));
        Assert.assertTrue(storyService.getStoryById((long) 1, userPrincipal).getBody().contains("<p>Body</p>"));
        Assert.assertTrue(storyService.getStoryById((long) 1, userPrincipal).getDescription().contains("Story Description"));
        Assert.assertNotNull(storyService.getStoryById((long) 1, userPrincipal).getCreatedBy());
        Assert.assertNotNull(storyService.getStoryById((long) 1, userPrincipal).getId());
        Assert.assertNotNull(storyService.getStoryById((long) 1, userPrincipal).getTotalLoves());

    }

    @Test
    public void should_return_getAllStories_method() throws Exception {


        User user = new User((long) 1, "Alibanana", "alibana123", "alibanana@gmail.com", "password");
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Pageable pageable = PageRequest.of(0, 30, Sort.Direction.DESC, "createdAt");
        Page<Story> stories = Mockito.mock(Page.class);
        when(storyRepository.findAll(isA(Pageable.class))).thenReturn(stories);
        Assert.assertNotNull(storyService.getAllStories(userPrincipal, 0, 30).getContent());
        Assert.assertEquals(0, storyService.getAllStories(userPrincipal, 0, 30).getPage());
        Assert.assertEquals(0, storyService.getAllStories(userPrincipal, 0, 30).getSize());
        Assert.assertEquals(0, storyService.getAllStories(userPrincipal, 0, 30).getTotalElement());
        Assert.assertEquals(0, storyService.getAllStories(userPrincipal, 0, 30).getTotalPages());
        Assert.assertEquals(false, storyService.getAllStories(userPrincipal, 0, 30).isLast());



    }

    @Test
    public void should_return_getStoriesCreatedBy_method() throws Exception {
        User user = new User((long) 2, "Antek Pierdoła", "pierdoła123", "pierdoła@gmail.com", "password");
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Pageable pageable = PageRequest.of(0, 30, Sort.Direction.DESC, "createdAt");
        Page<Story> stories = Mockito.mock(Page.class);
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(storyRepository.findByCreatedBy(any(Long.class), isA(Pageable.class))).thenReturn(stories);
        Assert.assertEquals(0, storyService.getStoriesCreatedBy(user.getUsername(), userPrincipal, 0, 30).getPage());
        Assert.assertEquals(false, storyService.getStoriesCreatedBy(user.getUsername(), userPrincipal, 0, 30).isLast());
        Assert.assertEquals(0, storyService.getStoriesCreatedBy(user.getUsername(), userPrincipal, 0, 30).getTotalPages());
        Assert.assertEquals(0, storyService.getStoriesCreatedBy(user.getUsername(), userPrincipal, 0, 30).getSize());
    }

    /*

    @Test
    public void should_return_getStoriesLovedBy_method() throws Exception {
        Instant createdAt = new SimpleDateFormat("yyyy-MM-dd").parse("2020-12-31").toInstant();
        Topic topic  = new Topic("Topic Name", "Topic Description");
        topic.setCreatedAt(createdAt);
        topic.setUpdatedAt(createdAt);
        topic.setId((long) 123);
        topic.setCreatedBy((long) 12);
        topic.setCreatedAt(createdAt);
        topic.setUpdatedAt(createdAt);
        Story story = new Story("dasdasdasd", "dsadasdsad", "dsadasdasgf a rt er ");

        story.setCreatedBy((long) 123);
        story.setId((long) 333);
        story.setTopic(topic);
        story.setUpdatedAt(createdAt);
        story.setCreatedAt(createdAt);


        User user = new User((long ) 3232332,"John Doe", "johndoe123", "joedoe@gmail.com", "password");
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(createdAt);

        Love love = new Love(story, user);
        love.setId(new Random().nextLong());

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(0, 30, Sort.Direction.DESC, "createdAt");
        List<Story> storiesList = new ArrayList<>();
        Page<Long> lovesList = Mockito.mock(Page.class);
        storiesList.add(story);
        List<Long> listOfLoves = new ArrayList<>();
        listOfLoves.add((long) 53412);
        listOfLoves.add((long) 1323232);
        listOfLoves.add((long) 13434);
        int total = listOfLoves.size();
        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min(start + pageable.getPageSize(), total);

        List<Long> output = new ArrayList<>();
        output = listOfLoves.subList(start, end);
        PageImpl page = new PageImpl<>(output, pageable, total);
        List<Love> loves = new ArrayList<>();
        loves.add(love);
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(loveRepository.findLoveStoryIdsByUserId(any(Long.class), isA(Pageable.class))).thenReturn(page);
        when(storyRepository.findByIdIn(any(List.class), isA(Sort.class))).thenReturn(storiesList);
        when(loveRepository.countByUserId(any(Long.class))).thenReturn((story.getId()));
        when(loveRepository.findByUserIdAndStoryIdIn(any(Long.class), any(List.class))).thenReturn(loves);
        when(userRepository.findByIdIn(any(List.class))).thenReturn(users);
        Assert.assertNotNull(storyService.getStoriesLovedBy(user.getUsername(), userPrincipal, 0, 30));


    }


     */
    @Test
    public void should_return_create_method() throws Exception {
        Topic topic  = new Topic("Topic Name", "Topic Description");
        Story story = new Story("Story Title", "Story Body", "Description");
        story.setTopic(topic);
        User user = new User("Antek Karwasz", "karwasz123", "karwasz@gmail.com", "password");
        StoryRequest storyRequest = new StoryRequest(story.getTitle(), story.getBody(), story.getDescription());
        when(topicRepository.findById(any(Long.class))).thenReturn(Optional.of(topic));
        when(storyRepository.save(any(Story.class))).thenReturn(story);
        Assert.assertTrue(storyService.createStory(storyRequest, (long) 1).getTitle().contains(story.getTitle()));
        Assert.assertTrue(storyService.createStory(storyRequest, (long) 1).getBody().contains(story.getBody()));
        Assert.assertTrue(storyService.createStory(storyRequest, (long) 1).getDescription().contains(story.getDescription()));
        Assert.assertTrue(storyService.createStory(storyRequest, (long) 23).getTopic().getDescription().contains(topic.getDescription()));
        Assert.assertTrue(storyService.createStory(storyRequest, (long) 23).getTopic().getTitle().contains(topic.getTitle()));
    }

    @Test
    public void should_return_castLoveAndGetUpdateStory_method() throws Exception {
        Instant createdAt = new SimpleDateFormat("yyyy-MM-dd").parse("2020-12-31").toInstant();
        Topic topic  = new Topic("Topic Name", "Topic Description");
        topic.setCreatedBy((long) 12);
        topic.setCreatedAt(createdAt);
        topic.setUpdatedAt(createdAt);
        Story story = new Story("Story Title", "Story Body", "Description");
        story.setCreatedBy((long) 123);
        story.setId((long) 1243);
        story.setTopic(topic);
        story.setCreatedAt(createdAt);
        story.setUpdatedAt(createdAt);
        User user = new User((long ) 10020,"John Doe", "johndoe123", "joedoe@gmail.com", "password");
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Love love = new Love(story, user);
        when(userRepository.getOne(any(Long.class))).thenReturn(user);
        when(storyRepository.findById(any(Long.class))).thenReturn(Optional.of(story));
        when(loveRepository.save(any(Love.class))).thenReturn(love);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(loveRepository.countByStoryId(any(Long.class))).thenReturn(story.getCreatedBy());
        when(loveRepository.countByStoryId(any(Long.class))).thenReturn((long) 123);
        Assert.assertTrue(storyService.castLoveAndGetUpdateStory((long) 123,userPrincipal).getBody().contains("Story Body"));
        Assert.assertTrue(storyService.castLoveAndGetUpdateStory((long) 12434324, userPrincipal).getDescription().contains(story.getDescription()));
        Assert.assertTrue(storyService.castLoveAndGetUpdateStory((long) 12434324, userPrincipal).getTitle().contains(story.getTitle()));
        Assert.assertTrue(storyService.castLoveAndGetUpdateStory((long) 43346546, userPrincipal).getTopic().getTitle().contains(topic.getTitle()));
        Assert.assertTrue(storyService.castLoveAndGetUpdateStory((long) 43346546, userPrincipal).getTopic().getDescription().contains(topic.getDescription()));
        Assert.assertEquals(createdAt, storyService.castLoveAndGetUpdateStory((long) 43346546, userPrincipal).getTopic().getcreatedAt());
        Assert.assertEquals(topic.getId(), storyService.castLoveAndGetUpdateStory((long) 43346546, userPrincipal).getTopic().getId());





    }

    @Test
    public void should_return_getStoryByTopicId_method() throws Exception {
        Instant createdAt = new SimpleDateFormat("yyyy-MM-dd").parse("2020-12-31").toInstant();
        Topic topic  = new Topic("Topic Name", "Topic Description");
        topic.setCreatedAt(createdAt);
        topic.setUpdatedAt(createdAt);
        topic.setId((long) 123);
        Story story = new Story("Story Title", "Story Body", "Description");
        story.setCreatedBy((long) 233);
        story.setId((long) 233);
        story.setTopic(topic);
        story.setCreatedAt(createdAt);
        story.setUpdatedAt(createdAt);
        Pageable pageable = PageRequest.of(0, 30, Sort.Direction.DESC, "createdAt");
        List<Story> storiesList = new ArrayList<>();
        storiesList.add(story);
        User user = new User((long ) 1223343,"Edvard More", "edvardmore123", "edvardmore@gmail.com", "password");
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        PageImpl<Story> page = createMockPage(storiesList);
        when(loveRepository.countByStoryId(any(Long.class))).thenReturn(new Random().nextLong());
        when(topicRepository.findById((any(Long.class)))).thenReturn(Optional.of(topic));
        when(storyRepository.findByTopicId(any(Long.class), isA(Pageable.class))).thenReturn(page);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        Assert.assertEquals(1, storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getContent().size());
        Assert.assertEquals(1, storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getTotalPages());
        Assert.assertEquals(0, storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getPage());
        Assert.assertEquals(1, storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getTotalElement());
        Assert.assertTrue(storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getContent().get(0).getTitle().contains(story.getTitle()));
        Assert.assertTrue(storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getContent().get(0).getTopic().getTitle().contains(topic.getTitle()));
        Assert.assertTrue(storyService.getStoryByTopicId(topic.getId(), userPrincipal, 0, 30).getContent().get(0).getTopic().getDescription().contains(topic.getDescription()));


    }

    private PageImpl createMockPage(List<Story> list) {
        PageRequest pageRequest = PageRequest.of(0, 30, Sort.Direction.DESC, "createdAt");
        int total = list.size();
        int start = Math.toIntExact(pageRequest.getOffset());
        int end = Math.min(start + pageRequest.getPageSize(), total);

        List<Story> output = new ArrayList<>();

        if(start <= end) {
            output = list.subList(start, end);
        }

        return new PageImpl<>(output, pageRequest, total);
     }


}
