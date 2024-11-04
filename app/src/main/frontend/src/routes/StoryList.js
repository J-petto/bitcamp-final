import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom'; // useNavigate import 추가
// import './StoryList.css'; // 스타일 파일 임포트
import axios from 'axios'; // axios를 import하여 API 요청 사용


const MyStoryList = () => {
    const [storyList, setStoryList] = useState([]); // 변수 이름을 stories로 수정
    const [accessToken, setAccessToken] = useState(null);
    const navigate = useNavigate(); // navigate 함수를 사용하여 페이지 이동


    // 로컬 스토리지에서 accessToken을 가져오는 함수
    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            setAccessToken(token);
        } else {
            console.warn("Access token이 없습니다.");
        }
    }, []);


    // accessToken이 설정된 경우에만 fetchList 호출
    useEffect(() => {
        if (accessToken) {
            const fetchStoryList = async () => {
                try {
                    const response = await axios.get('http://localhost:8080/my-story/list', {
                        headers: {
                            'Authorization': `Bearer ${accessToken}`
                        }
                    }); // API 요청
                    setStoryList(response.data);
                } catch (error) {
                    console.error("There was an error", error);
                }
            };
            fetchStoryList();
        }
    }, [accessToken]);


    // 스토리 추가 버튼 클릭 시 MyStoryAddForm으로 이동하는 함수
    const handleAddStory = () => {
        navigate('/my-story/form/add'); // MyStoryAddForm 페이지로 이동
    };

    return (
        <div className="story-list">
            <h2>My 스토리</h2>
            <ul>
                <li className="story-card add-story-card" onClick={handleAddStory}>
                    <div className="add-story-icon">+</div> {/* 버튼 대신 아이콘을 div로 만듦 */}
                </li>
                {Array.isArray(storyList) && storyList.map((storyListDTO) => (
                    <li key={storyListDTO.storyId} className="story-card">
                        <div className="story-header">
                            <p className="nickname">{storyListDTO.userNickname}</p>
                            <button className="share-button">{storyListDTO.share ? '공유됨' : '공유하기'}</button>
                        </div>
                        {storyListDTO.mainPhoto && (
                            <Link to={`/my-story/view/${storyListDTO.storyId}`}>
                                <div className="image-container">
                                    <img src={`https://kr.object.ncloudstorage.com/bitcamp-bucket-final/story/${storyListDTO.mainPhoto.path ? storyListDTO.mainPhoto.path : 'default.png'}`} />
                                </div>
                            </Link>
                        )}
                        <div className="story-info">
                            <div className="like-info">
                                <button className="like-button">{storyListDTO.likeStatus ? '좋아요함' : '좋아요안함'}</button>
                                <span>{storyListDTO.likeCount}</span>
                            </div>
                            <Link to={`/my-story/view/${storyListDTO.storyId}`}>{storyListDTO.title}</Link>
                            <Link to={`/my-story/view/${storyListDTO.storyId}`}>
                                <p>{storyListDTO.content}</p>
                            </Link>
                            <div className="location-date">
                                <p>{storyListDTO.locationFirstName} {storyListDTO.locationDetail}</p>
                                <p>{storyListDTO.travelDate}</p>
                            </div>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default MyStoryList;
