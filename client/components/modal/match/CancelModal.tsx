import { useEffect } from 'react';
import { useRecoilState, useSetRecoilState } from 'recoil';
import { Cancel } from 'types/modalTypes';
import instance from 'utils/axios';
import { errorState } from 'utils/recoil/error';
import {
  currentMatchState,
  openCurrentMatchState,
  reloadMatchState,
} from 'utils/recoil/match';
import { modalState } from 'utils/recoil/modal';
import styles from 'styles/modal/CancelModal.module.scss';

export default function CancelModal({ slotId }: Cancel) {
  const setOpenCurrentMatch = useSetRecoilState(openCurrentMatchState);
  const setReloadMatch = useSetRecoilState(reloadMatchState);
  const setError = useSetRecoilState(errorState);
  const setModal = useSetRecoilState(modalState);
  const [currentMatch, setCurrentMatch] = useRecoilState(currentMatchState);
  const content = {
    emoji: '๐ค',
    main: 'ํด๋น ๊ฒฝ๊ธฐ๋ฅผ\n์ทจ์ํ์๊ฒ ์ต๋๊น?',
  };
  const cancelResponse: { [key: string]: string } = {
    SUCCESS: '๊ฒฝ๊ธฐ๊ฐ ์ฑ๊ณต์ ์ผ๋ก ์ทจ์๋์์ต๋๋ค.',
    SD001: '์ด๋ฏธ ์ง๋ ๊ฒฝ๊ธฐ์๋๋ค.',
    SD002: '์ด๋ฏธ ๋งค์นญ์ด ์๋ฃ๋ ๊ฒฝ๊ธฐ์๋๋ค.',
    E0001: '์๋ชป๋ ์์ฒญ์๋๋ค.',
  };

  useEffect(() => {
    getCurrentMatchHandler();
  }, []);

  const onCancel = async () => {
    try {
      await instance.delete(`/pingpong/match/slots/${slotId}`);
      alert(cancelResponse.SUCCESS);
    } catch (e: any) {
      if (e.response.data.code in cancelResponse)
        alert(cancelResponse[e.response.data.code]);
      else {
        setModal({ modalName: null });
        setOpenCurrentMatch(false);
        setError('JH01');
        return;
      }
    }
    setModal({ modalName: null });
    setOpenCurrentMatch(false);
    setReloadMatch(true);
  };

  const getCurrentMatchHandler = async () => {
    try {
      const res = await instance.get('/pingpong/match/current');
      setCurrentMatch(res.data);
    } catch (e) {
      setError('JH08');
    }
  };

  const onReturn = () => {
    setModal({ modalName: null });
    setReloadMatch(true);
  };

  return (
    <div className={styles.container}>
      <div className={styles.phrase}>
        <div className={styles.emoji}>{content.emoji}</div>
        {content.main}
        {<div className={styles.subContent}></div>}
      </div>
      <div className={styles.buttons}>
        {
          <>
            <div className={styles.negative}>
              <input onClick={onReturn} type='button' value='์๋์ค' />
            </div>
            <div className={styles.positive}>
              <input onClick={onCancel} type='button' value='์' />
            </div>
          </>
        }
      </div>
    </div>
  );
}
