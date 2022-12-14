import { useRouter } from 'next/router';
import { useSetRecoilState } from 'recoil';
import { loginState } from 'utils/recoil/login';
import { modalState } from 'utils/recoil/modal';
import styles from 'styles/modal/LogoutModal.module.scss';

export default function LogoutModal() {
  const setIsLoggedIn = useSetRecoilState(loginState);
  const setModal = useSetRecoilState(modalState);
  const router = useRouter();

  const onReturn = () => {
    setModal({ modalName: null });
  };

  const onLogout = () => {
    localStorage.removeItem('42gg-token');
    setIsLoggedIn(false);
    router.push(`/`);
  };

  return (
    <div className={styles.container}>
      <div className={styles.phrase}>
        <div className={styles.emoji}>π₯²</div>
        <div>
          λ‘κ·Έμμ
          <br />
          νμκ² μ΅λκΉ?
        </div>
      </div>
      <div className={styles.buttons}>
        <div className={styles.negative}>
          <input onClick={onReturn} type='button' value='μλμ€' />
        </div>
        <div className={styles.positive}>
          <input onClick={onLogout} type='button' value='μ' />
        </div>
      </div>
    </div>
  );
}
