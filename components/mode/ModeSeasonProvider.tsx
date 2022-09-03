import React from 'react';
import { useEffect, useState } from 'react';
import { useRecoilValue } from 'recoil';
import { UserData } from 'types/mainType';
import { userState } from 'utils/recoil/layout';
import { seasonState } from 'utils/recoil/seasons';
import ModeToggle from './ModeToggle';
import SeasonDropDown from './SeasonDropDown';
import styles from 'styles/mode/ModeSelect.module.scss';

interface ModeSelectProps {
  children: React.ReactNode;
  setModeProps: React.Dispatch<React.SetStateAction<string>>;
}

export default function ModeSeasonProvider({
  children,
  setModeProps,
}: ModeSelectProps) {
  const userData = useRecoilValue<UserData>(userState);
  const [mode, setMode] = useState(userData?.mode);
  const [season, setSeason] = useState('');
  const [displaySeasons, setDisplaySeasons] = useState(true);
  const seasonList = useRecoilValue(seasonState);

  useEffect(() => {
    setDisplaySeasons(mode === 'rank');
    setModeProps(mode);
  }, [mode]);

  const modeToggleHandler = () => {
    setMode((mode) => (mode === 'rank' ? 'normal' : 'rank'));
  };

  const seasonDropDownHandler = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSeason(e.target.value);
  };

  return (
    <div>
      <div className={styles.wrapper}>
        <ModeToggle
          checked={mode === 'rank'}
          onToggle={modeToggleHandler}
          text={mode === 'rank' ? '랭크' : '일반'}
        />
        {displaySeasons && seasonList && (
          <SeasonDropDown
            seasons={seasonList}
            value={season}
            onSelect={seasonDropDownHandler}
          />
        )}
      </div>
      {React.cloneElement(children as React.ReactElement, {
        mode,
        season,
      })}
    </div>
  );
}
