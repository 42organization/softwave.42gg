import { useState } from 'react';
import { useSetRecoilState } from 'recoil';
import { errorState } from 'utils/recoil/error';
import instance from 'utils/axios';
import styles from '/styles/modal/MatchTriggerModal.module.scss';

interface SlotId {
  slotId: number;
}

export default function MatchTriggerModal() {
  const [slotId, setSlotId] = useState<SlotId>({
    slotId: 0,
  });
  const setError = useSetRecoilState(errorState);
  const matchTriggerResponse: { [key: string]: string } = {
    E0001: '잘못된 요청입니다.',
  };

  const inputChangeHandler = ({
    target: { value },
  }: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (isNaN(Number(value))) {
      setSlotId({ slotId: 0 });
    } else {
      setSlotId({ slotId: Number(value) });
    }
  };

  const matchTriggerHandler = async () => {
    if (slotId.slotId) {
      try {
        await instance.post('/pingpong/admin/matchtrigger', slotId);
        alert('매치 시작 요청 완료 !');
      } catch (e: any) {
        if (e.response.data.code in matchTriggerResponse) {
          alert(matchTriggerResponse[e.response.data.code]);
        } else {
          setError('RJ01');
        }
      }
    } else {
      alert('슬롯 아이디(숫자)를 입력해 주세요 !');
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.title}>Match Trigger</div>
      <form>
        <div className={styles.slotId}>
          <textarea
            name='slotId'
            onChange={inputChangeHandler}
            placeholder='슬롯 id'
          />
        </div>
        <div className={styles.buttons}>
          <div className={styles.positive}>
            <input type='button' onClick={matchTriggerHandler} value='요청' />
          </div>
        </div>
      </form>
    </div>
  );
}
