package bitcamp.project.controller;

import bitcamp.project.service.LocationService;
import bitcamp.project.service.StoryService;
import bitcamp.project.service.UserService;
import bitcamp.project.service.impl.FileServiceImpl;
import bitcamp.project.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/story")
public class StoryController {

    private final StoryService storyService;
    private final LocationService locationService;
    private final UserService userService;

    @GetMapping("list")
    public ResponseEntity<List<Map<String, Object>>> list() throws Exception {

        List<Map<String, Object>> responseList = new ArrayList<>();

        // Story 1개 + Main 사진 1개
        for (Story story : storyService.list()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("story", story);

            for (Photo photo : storyService.getPhotos(story.getId())) {
                if (photo.isMainPhoto()) {
                    map.put("mainPhoto", photo);
                }
            }

            responseList.add(map);
        }

        return ResponseEntity.ok(responseList);
    }


    @GetMapping("view/{storyId}")
    public ResponseEntity<Map<String, Object>> view(@PathVariable  int storyId) throws Exception {
        Story story = storyService.get(storyId);
        if (story == null) {
            throw new Exception("스토리가 존재하지 않습니다.");
        }

        List<Photo> photos = storyService.getPhotos(storyId);

        // Story와 함께 Photo 데이터 보내기
        Map<String, Object> response = new HashMap<>();
        response.put("story", story);
        response.put("photos", photos);

        return ResponseEntity.ok(response);
    }


    @GetMapping("form")
    public void form() {
    }


    @DeleteMapping("delete/{storyId}")
    public void delete(@PathVariable int storyId) throws Exception {

        Story story = storyService.get(storyId);
        if (story == null) {
            throw new Exception("스토리가 존재하지 않습니다.");
        }


        FileServiceImpl fileServiceImpl = new FileServiceImpl();

        for (Photo photo : storyService.getPhotos(storyId)) {
            try {
                // 첨부파일 삭제
                fileServiceImpl.deletePhoto(photo.getPath());

            } catch (Exception e) {
                System.out.printf("%s 파일 ", photo.getPath());
                throw new Exception("삭제 실패 !");
            }
        }

        storyService.delete(storyId);
    }


    @PostMapping("add/{firstName}/{secondName}")
    public ResponseEntity<Map<String, Object>> add(
        @ModelAttribute Story story,
        MultipartFile[] files,
        @PathVariable String firstName, @PathVariable String secondName) throws Exception {

        // 로그인 사용자
        User user = userService.findUser(4);

        // 위치 정보
        Location location = locationService.findByLocation(firstName, secondName);
        if (location == null) {
            throw new Exception("위치 정보 없음");
        }

        // Story에 로그인 사용자 및 위치 정보 삽입
        story.setLocation(location);
        story.setUser(user);
        storyService.add(story);

        // Photo 정보
        List<Photo> photos = new ArrayList<>();
        FileServiceImpl fileServiceImpl = new FileServiceImpl();

        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                continue;
            }

            // 첨부파일 저장
            String fileName = fileServiceImpl.upload(file);

            Photo photo = new Photo();
            photo.setStoryId(story.getId());
            photo.setMainPhoto(false);
            photo.setPath("/photos/" + fileName);

            photos.add(photo);
        }

        // Photo DB 처리
        storyService.addPhotos(photos);

        Map<String, Object> response = new HashMap<>();
        response.put("story", story);
        response.put("photos", photos);

        return ResponseEntity.ok(response);
    }


    @PostMapping("update/{firstName}/{secondName}")
    public ResponseEntity<Map<String, Object>> update(
        @ModelAttribute Story story,
        MultipartFile[] files,
        @PathVariable String firstName, @PathVariable String secondName) throws Exception {

        User user = userService.findUser(4);

        Location location = locationService.findByLocation(firstName, secondName);
        if (location == null) {
            throw new Exception("위치 정보 없음");
        }

        story.setLocation(location);
        story.setUser(user);
        storyService.update(story);

        // Photo 정보
        List<Photo> photos = new ArrayList<>();
        FileServiceImpl fileServiceImpl = new FileServiceImpl();

        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                continue;
            }

            // 첨부파일 저장
            String fileName = fileServiceImpl.upload(file);

            Photo photo = new Photo();
            photo.setStoryId(story.getId());
            photo.setMainPhoto(false);
            photo.setPath("/photos/" + fileName);

            photos.add(photo);
        }

        // Photo DB 처리
        storyService.addPhotos(photos);

        Map<String, Object> response = new HashMap<>();
        response.put("story", story);
        response.put("photos", photos);

        return ResponseEntity.ok(response);
    }


    @GetMapping("share/{storyId}")
    public Story share(@PathVariable int storyId) throws Exception {
        Story story = storyService.get(storyId);
        if (story == null) {
            throw new Exception("스토리 정보 없음");
        }

        story.setShare(!story.isShare());
        storyService.update(story);
        return story;
    }


    @DeleteMapping("photo/delete/{photoId}")
    public void photoDelete(@PathVariable int photoId) throws Exception {

        // 삭제할 Photo 가져오기
        Photo photo = storyService.getPhoto(photoId);
        if (photo == null) {
            throw new Exception("없는 사진입니다.");
        }

        // Story에서 로그인 사용자 정보를 가져와서 권한 파악 시 필요 (지금 X)
//        Story story = storyService.get(photo.getStoryId());
//        if (story == null) {
//            throw new Exception("없는 스토리입니다.");
//        }

        // Photo 파일 삭제
        try {
            FileServiceImpl fileServiceImpl = new FileServiceImpl();
            fileServiceImpl.deletePhoto(photo.getPath());

        } catch (Exception e) {
            throw new Exception("파일 삭제 실패로 인한 DB 삭제 중단");
        }

        // Photo DB 삭제
        storyService.deletePhoto(photoId);
    }

}
