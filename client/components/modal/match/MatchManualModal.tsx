import { useSetRecoilState } from 'recoil';
import { modalState } from 'utils/recoil/modal';
import styles from 'styles/modal/MatchManualModal.module.scss';

export default function MatchManualModal() {
  const setModal = useSetRecoilState(modalState);

  const onReturn = () => {
    setModal({ modalName: null });
  };

  return (
    <div className={styles.container}>
      <div className={styles.title}>Please!!</div>
      <ul className={styles.ruleList}>
        {modalContentsRank.map(
          (item: { title: string; description: string[] }, index) => (
            <li key={index}>
              {item.title}
              <ul className={styles.ruleDetail}>
                {item.description.map((e, idx) => (
                  <li key={idx}>{e}</li>
                ))}
              </ul>
            </li>
          )
        )}
      </ul>
      <div className={styles.buttons}>
        <div className={styles.positive}>
          <input onClick={onReturn} type='button' value={'ํ ์ธ'} />
        </div>
      </div>
    </div>
  );
}

const modalContentsRank: { title: string; description: string[] }[] = [
  {
    title: '๐ ๋งค์นญ',
    description: [
      '๋ฑ๋กํ ๊ฒฝ๊ธฐ๊ฐ ๋๋์ผ๋ง ๋ค์ ๊ฒฝ๊ธฐ ๋ฑ๋ก ๊ฐ๋ฅ',
      '๋งค์นญ์ด ์๋ฃ๋๋ฉด ์๋๋ฐฉ์ด ๊ณต๊ฐ๋๋ฉฐ ๋ถ์ค๋ก ๋ฐฉ๋ฌธํ์ฌ ๊ฒฝ๊ธฐ ์งํ',
      '์๋๋ฐฉ์ด ๊ฒฝ๊ธฐ๋ฅผ ์ทจ์ํ๋ฉด ๋งค์นญ ๋๊ธฐ ์ํ๋ก ์ ํ',
    ],
  },
  {
    title: '๐ 42gg์ ํจ๊ปํ๊ธฐ',
    description: [
      '์ฌ๋กฏ์ด ๋น์ด์๋ ์ํ์์๋ง ๋ฑ๋ก ๊ฐ๋ฅ',
      '๋น ์ฌ๋กฏ์ ๋๋ฅด๊ณ  ๋๊ฒฐํ  ๋งด๋ฒ๋ฅผ ์ ํ',
      '๋ถ์ค๋ก ๋ฐฉ๋ฌธํ์ฌ ๊ฒฝ๊ธฐ ์งํ',
    ],
  },
  {
    title: 'โ ๊ฒฝ๊ธฐ ๋ฃฐ',
    description: [
      '๊ฒฝ๊ธฐ๋ ๋จํ, 5์  ๋ด๊ธฐ',
      '๊ฒฝ๊ธฐ๊ฐ ์ข๋ฃ๋ ํ ์ด๊ธด ์ฌ๋์ด ์ ์ ์๋ ฅ',
      '์๋ ฅ ์  ์ํธ ๊ฐ ํ์ธ์ด ์ด๋ฃจ์ด์ง๋ฏ๋ก ์๋ชป ๊ธฐ์ ์ ์ํธ ์ฑ์',
    ],
  },
  {
    title: '๐จ ๋ธ์ผ',
    description: [`์๋๋ฐฉ์ด ๋ํ๋์ง ์์ ๊ฒฝ์ฐ staff์๊ฒ ๋ง์ํด์ฃผ์ธ์!`],
  },
];
