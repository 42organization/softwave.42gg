import { useRecoilState, useSetRecoilState } from 'recoil';
import { MatchMode } from 'types/mainType';
import { myRankState, pageState } from 'utils/recoil/myRank';
import styles from 'styles/rank/RankList.module.scss';

interface MyRankProps {
  toggleMode: MatchMode;
}

export default function MyRank({ toggleMode }: MyRankProps) {
  const [myRank, setMyRank] = useRecoilState(myRankState);
  const setPage = useSetRecoilState(pageState);
  const rankType = toggleMode === 'rank' ? 'λ­ν¬' : 'μ΄μ ';
  const isRanked = myRank[toggleMode] === -1 ? 'unrank' : 'rank';
  const content = {
    unrank: {
      style: '',
      message: [
        `π‘ λμ ${
          rankType + (toggleMode === 'rank' ? 'κ°' : 'μ΄')
        } μ ν΄μ§μ§ μμμ΅λλ€ π‘`,
      ],
    },
    rank: {
      style: styles.rank,
      message: [
        `ππ λμ ${rankType}`,
        ` ${myRank[toggleMode]}μ`,
        ' λ°λ‘κ°κΈ° ππ',
      ],
    },
  };

  const myRankHandler = () => {
    if (myRank[toggleMode] === -1) return;
    setMyRank((prev) => ({ ...prev, clicked: true }));
    setPage(Math.ceil(myRank[toggleMode] / 20));
  };

  return (
    <div
      className={`${styles.myRank} ${content[isRanked].style}`}
      onClick={myRankHandler}
    >
      {myRank[toggleMode] !== 0 &&
        content[isRanked].message.map((e, index) => (
          <span key={index}>{e}</span>
        ))}
    </div>
  );
}
