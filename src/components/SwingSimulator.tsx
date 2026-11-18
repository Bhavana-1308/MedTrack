import React, { useEffect, useMemo, useState } from 'react';
import {
  Bell,
  Check,
  Download,
  Edit2,
  FileText,
  LogOut,
  Plus,
  RotateCcw,
  Search,
  Trash2,
  X,
} from 'lucide-react';

interface DemoUser {
  id: number;
  fullName: string;
  username: string;
  email: string;
  phone: string;
  password: string;
}

interface DemoMedicine {
  id: number;
  userId: number;
  name: string;
  type: 'PRESCRIPTION' | 'OTC';
  dosage: string;
  frequency: string;
  startDate: string;
  endDate: string;
  doctorName: string;
  purposeCategory: string;
  notes: string;
}

interface DemoReminder {
  id: number;
  medicineId: number;
  userId: number;
  time: string;
  label: string;
  active: boolean;
}

interface DemoHistory {
  id: number;
  medicineId: number;
  reminderId: number | null;
  userId: number;
  doseDate: string;
  scheduledTime: string;
  takenAt: string | null;
  status: 'TAKEN' | 'MISSED';
  remarks: string;
}

interface DemoStore {
  users: DemoUser[];
  medicines: DemoMedicine[];
  reminders: DemoReminder[];
  history: DemoHistory[];
  logs: string[];
}

const STORAGE_KEY = 'medtrack_java_pbl_demo_v1';

function todayIso(offsetDays = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + offsetDays);
  return d.toISOString().slice(0, 10);
}

function nowStamp(): string {
  return new Date().toISOString().replace('T', ' ').slice(0, 19);
}

function createSeedStore(): DemoStore {
  return {
    users: [
      {
        id: 1,
        fullName: 'Arjun Sharma',
        username: 'arjun_med',
        email: 'arjun.sharma@example.edu',
        phone: '9876543210',
        password: 'MedTrack@123',
      },
    ],
    medicines: [
      {
        id: 1,
        userId: 1,
        name: 'Amoxicillin',
        type: 'PRESCRIPTION',
        dosage: '500 mg',
        frequency: 'Twice Daily',
        startDate: todayIso(0),
        endDate: todayIso(7),
        doctorName: 'Dr. Meera Nair',
        purposeCategory: '',
        notes: 'Take after meals with water',
      },
      {
        id: 2,
        userId: 1,
        name: 'Vitamin D3',
        type: 'OTC',
        dosage: '1000 IU',
        frequency: 'Once Daily',
        startDate: todayIso(0),
        endDate: todayIso(30),
        doctorName: '',
        purposeCategory: 'Nutritional Supplement',
        notes: 'Take with breakfast',
      },
    ],
    reminders: [
      { id: 1, medicineId: 1, userId: 1, time: '08:30', label: 'After Breakfast', active: true },
      { id: 2, medicineId: 1, userId: 1, time: '20:30', label: 'After Dinner', active: true },
      { id: 3, medicineId: 2, userId: 1, time: '09:00', label: 'Morning Supplement', active: true },
    ],
    history: [
      {
        id: 1,
        medicineId: 1,
        reminderId: 1,
        userId: 1,
        doseDate: todayIso(0),
        scheduledTime: '08:30',
        takenAt: nowStamp(),
        status: 'TAKEN',
        remarks: 'Taken on time after breakfast',
      },
      {
        id: 2,
        medicineId: 2,
        reminderId: 3,
        userId: 1,
        doseDate: todayIso(-1),
        scheduledTime: '09:00',
        takenAt: null,
        status: 'MISSED',
        remarks: 'Forgot during morning commute',
      },
    ],
    logs: [
      `[${nowStamp()}] [arjun_med] Seeded initial demo database records.`,
      `[${nowStamp()}] [arjun_med] Marked Amoxicillin (08:30) as TAKEN`,
    ],
  };
}

function getDosageInstructions(med: DemoMedicine): string {
  if (med.type === 'PRESCRIPTION') {
    const doc = med.doctorName.trim() || 'your physician';
    return `Prescription required (${doc}). Complete full course as directed: ${med.dosage}, ${med.frequency}.`;
  }
  const cat = med.purposeCategory.trim() || 'General Care';
  return `Over-the-Counter (${cat}). Follow label limits: ${med.dosage}, ${med.frequency}.`;
}

export const SwingSimulator: React.FC = () => {
  const [store, setStore] = useState<DemoStore>(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) return JSON.parse(raw) as DemoStore;
    } catch {
      // ignore storage errors
    }
    return createSeedStore();
  });

  const [currentUserId, setCurrentUserId] = useState<number | null>(1);
  const [authScreen, setAuthScreen] = useState<'login' | 'register'>('login');
  const [loginIdentifier, setLoginIdentifier] = useState('arjun_med');
  const [loginPassword, setLoginPassword] = useState('MedTrack@123');
  const [authError, setAuthError] = useState('');
  const [authSuccess, setAuthSuccess] = useState('');

  // Registration form
  const [regName, setRegName] = useState('');
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regConfirm, setRegConfirm] = useState('');

  // Dashboard tabs
  const [activeTab, setActiveTab] = useState<'today' | 'medicines' | 'reminders' | 'history'>('today');
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<'ALL' | 'PRESCRIPTION' | 'OTC'>('ALL');

  // Modals & notifications
  const [medModalOpen, setMedModalOpen] = useState(false);
  const [editingMed, setEditingMed] = useState<DemoMedicine | null>(null);
  const [remModalOpen, setRemModalOpen] = useState(false);
  const [guideMed, setGuideMed] = useState<DemoMedicine | null>(null);
  const [activeAlert, setActiveAlert] = useState<{ reminder: DemoReminder; medicine: DemoMedicine } | null>(null);
  const [lastTriggeredMinute, setLastTriggeredMinute] = useState<Record<number, string>>({});
  const [feedbackNotice, setFeedbackNotice] = useState<string>('');

  // Medicine modal state
  const [mName, setMName] = useState('');
  const [mType, setMType] = useState<'PRESCRIPTION' | 'OTC'>('PRESCRIPTION');
  const [mDosage, setMDosage] = useState('');
  const [mFreq, setMFreq] = useState('Once Daily');
  const [mStart, setMStart] = useState(todayIso(0));
  const [mEnd, setMEnd] = useState(todayIso(7));
  const [mDetail, setMDetail] = useState('');
  const [mNotes, setMNotes] = useState('');
  const [modalError, setModalError] = useState('');

  // Reminder modal state
  const [rMedId, setRMedId] = useState<number>(0);
  const [rTime, setRTime] = useState('08:30');
  const [rLabel, setRLabel] = useState('After Breakfast');
  const [rActive, setRActive] = useState(true);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(store));
    } catch {
      // ignore storage errors
    }
  }, [store]);

  const currentUser = useMemo(
    () => store.users.find((u) => u.id === currentUserId) ?? null,
    [store.users, currentUserId]
  );

  const userMedicines = useMemo(
    () => store.medicines.filter((m) => m.userId === currentUserId),
    [store.medicines, currentUserId]
  );

  const medicineMap = useMemo(() => {
    const map = new Map<number, DemoMedicine>();
    userMedicines.forEach((m) => map.set(m.id, m));
    return map;
  }, [userMedicines]);

  const userReminders = useMemo(
    () =>
      store.reminders
        .filter((r) => r.userId === currentUserId)
        .sort((a, b) => a.time.localeCompare(b.time)),
    [store.reminders, currentUserId]
  );

  const todayReminders = useMemo(() => {
    const today = todayIso(0);
    return userReminders.filter((r) => {
      if (!r.active) return false;
      const med = medicineMap.get(r.medicineId);
      return med ? today >= med.startDate && today <= med.endDate : false;
    });
  }, [userReminders, medicineMap]);

  const userHistory = useMemo(
    () =>
      store.history
        .filter((h) => h.userId === currentUserId)
        .sort((a, b) => `${b.doseDate} ${b.scheduledTime}`.localeCompare(`${a.doseDate} ${a.scheduledTime}`)),
    [store.history, currentUserId]
  );

  const filteredMedicines = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return userMedicines.filter((m) => {
      const matchesType = typeFilter === 'ALL' || m.type === typeFilter;
      const matchesQuery = !q || m.name.toLowerCase().includes(q) || m.dosage.toLowerCase().includes(q);
      return matchesType && matchesQuery;
    });
  }, [userMedicines, searchQuery, typeFilter]);

  const stats = useMemo(() => {
    const taken = userHistory.filter((h) => h.status === 'TAKEN').length;
    const missed = userHistory.filter((h) => h.status === 'MISSED').length;
    const activeRem = userReminders.filter((r) => r.active).length;
    return {
      totalMedicines: userMedicines.length,
      activeReminders: activeRem,
      taken,
      missed,
    };
  }, [userMedicines.length, userReminders, userHistory]);

  // Simulate background ReminderSchedulerThread checking system time every 10s
  useEffect(() => {
    if (!currentUser) return;
    const timer = window.setInterval(() => {
      const now = new Date();
      const hhmm = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
      for (const r of todayReminders) {
        if (r.time === hhmm && lastTriggeredMinute[r.id] !== hhmm) {
          const med = medicineMap.get(r.medicineId);
          if (med) {
            setLastTriggeredMinute((prev) => ({ ...prev, [r.id]: hhmm }));
            setActiveAlert({ reminder: r, medicine: med });
            break;
          }
        }
      }
    }, 10000);
    return () => window.clearInterval(timer);
  }, [currentUser, todayReminders, medicineMap, lastTriggeredMinute]);

  function appendLog(username: string, action: string) {
    return `[${nowStamp()}] [${username}] ${action}`;
  }

  function showNotice(msg: string) {
    setFeedbackNotice(msg);
    window.setTimeout(() => setFeedbackNotice(''), 3500);
  }

  function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setAuthError('');
    setAuthSuccess('');
    if (!loginIdentifier.trim() || !loginPassword.trim()) {
      setAuthError('Username/Email and Password cannot be empty.');
      return;
    }
    const found = store.users.find(
      (u) =>
        (u.username.toLowerCase() === loginIdentifier.trim().toLowerCase() ||
          u.email.toLowerCase() === loginIdentifier.trim().toLowerCase()) &&
        u.password === loginPassword
    );
    if (!found) {
      setAuthError('Invalid username/email or password.');
      return;
    }
    setStore((prev) => ({
      ...prev,
      logs: [...prev.logs, appendLog(found.username, 'User signed in.')],
    }));
    setCurrentUserId(found.id);
  }

  function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setAuthError('');
    if (!regName.trim() || !regUsername.trim() || !regEmail.trim()) {
      setAuthError('Full Name, Username, and Email are required.');
      return;
    }
    if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(regEmail.trim())) {
      setAuthError('Please enter a valid email address.');
      return;
    }
    if (regPassword.length < 6) {
      setAuthError('Password must be at least 6 characters long.');
      return;
    }
    if (regPassword !== regConfirm) {
      setAuthError('Passwords do not match.');
      return;
    }
    if (store.users.some((u) => u.username.toLowerCase() === regUsername.trim().toLowerCase())) {
      setAuthError(`Username '${regUsername.trim()}' is already taken.`);
      return;
    }
    if (store.users.some((u) => u.email.toLowerCase() === regEmail.trim().toLowerCase())) {
      setAuthError('An account with this email already exists.');
      return;
    }

    const nextId = store.users.reduce((max, u) => Math.max(max, u.id), 0) + 1;
    const newUser: DemoUser = {
      id: nextId,
      fullName: regName.trim(),
      username: regUsername.trim(),
      email: regEmail.trim(),
      phone: regPhone.trim(),
      password: regPassword,
    };
    setStore((prev) => ({
      ...prev,
      users: [...prev.users, newUser],
      logs: [...prev.logs, appendLog(newUser.username, 'New user account registered.')],
    }));
    setLoginIdentifier(newUser.username);
    setLoginPassword(newUser.password);
    setAuthScreen('login');
    setAuthSuccess('Account created successfully. Sign in with your new credentials.');
    setRegName('');
    setRegUsername('');
    setRegEmail('');
    setRegPhone('');
    setRegPassword('');
    setRegConfirm('');
  }

  function openAddMedicine() {
    setEditingMed(null);
    setMName('');
    setMType('PRESCRIPTION');
    setMDosage('');
    setMFreq('Once Daily');
    setMStart(todayIso(0));
    setMEnd(todayIso(7));
    setMDetail('');
    setMNotes('');
    setModalError('');
    setMedModalOpen(true);
  }

  function openEditMedicine(med: DemoMedicine) {
    setEditingMed(med);
    setMName(med.name);
    setMType(med.type);
    setMDosage(med.dosage);
    setMFreq(med.frequency);
    setMStart(med.startDate);
    setMEnd(med.endDate);
    setMDetail(med.type === 'PRESCRIPTION' ? med.doctorName : med.purposeCategory);
    setMNotes(med.notes);
    setModalError('');
    setMedModalOpen(true);
  }

  function handleSaveMedicine(e: React.FormEvent) {
    e.preventDefault();
    if (!currentUser) return;
    if (!mName.trim() || !mDosage.trim() || !mFreq.trim()) {
      setModalError('Medicine Name, Dosage, and Frequency are required.');
      return;
    }
    if (!mStart || !mEnd || mEnd < mStart) {
      setModalError('End Date cannot be earlier than Start Date.');
      return;
    }

    if (editingMed) {
      const updated: DemoMedicine = {
        ...editingMed,
        name: mName.trim(),
        type: mType,
        dosage: mDosage.trim(),
        frequency: mFreq.trim(),
        startDate: mStart,
        endDate: mEnd,
        doctorName: mType === 'PRESCRIPTION' ? mDetail.trim() : '',
        purposeCategory: mType === 'OTC' ? mDetail.trim() : '',
        notes: mNotes.trim(),
      };
      setStore((prev) => ({
        ...prev,
        medicines: prev.medicines.map((m) => (m.id === updated.id ? updated : m)),
        logs: [...prev.logs, appendLog(currentUser.username, `Updated medicine: ${updated.name} (${updated.dosage})`)],
      }));
      showNotice(`Updated ${updated.name}.`);
    } else {
      const nextId = store.medicines.reduce((max, m) => Math.max(max, m.id), 0) + 1;
      const created: DemoMedicine = {
        id: nextId,
        userId: currentUser.id,
        name: mName.trim(),
        type: mType,
        dosage: mDosage.trim(),
        frequency: mFreq.trim(),
        startDate: mStart,
        endDate: mEnd,
        doctorName: mType === 'PRESCRIPTION' ? mDetail.trim() : '',
        purposeCategory: mType === 'OTC' ? mDetail.trim() : '',
        notes: mNotes.trim(),
      };
      setStore((prev) => ({
        ...prev,
        medicines: [...prev.medicines, created],
        logs: [...prev.logs, appendLog(currentUser.username, `Added medicine: ${created.name} (${created.dosage})`)],
      }));
      showNotice(`Added ${created.name} to your medicine list.`);
    }
    setMedModalOpen(false);
  }

  function handleDeleteMedicine(med: DemoMedicine) {
    if (!currentUser) return;
    setStore((prev) => ({
      ...prev,
      medicines: prev.medicines.filter((m) => m.id !== med.id),
      reminders: prev.reminders.filter((r) => r.medicineId !== med.id),
      history: prev.history.filter((h) => h.medicineId !== med.id),
      logs: [...prev.logs, appendLog(currentUser.username, `Deleted medicine: ${med.name}`)],
    }));
    showNotice(`Deleted ${med.name} and its reminders.`);
  }

  function openAddReminder() {
    if (userMedicines.length === 0) {
      showNotice('Add at least one medicine before setting a reminder.');
      return;
    }
    setRMedId(userMedicines[0].id);
    setRTime('08:30');
    setRLabel('After Breakfast');
    setRActive(true);
    setModalError('');
    setRemModalOpen(true);
  }

  function handleSaveReminder(e: React.FormEvent) {
    e.preventDefault();
    if (!currentUser) return;
    if (!/^\d{2}:\d{2}$/.test(rTime)) {
      setModalError('Reminder Time must follow HH:mm 24-hour format.');
      return;
    }
    const nextId = store.reminders.reduce((max, r) => Math.max(max, r.id), 0) + 1;
    const med = medicineMap.get(rMedId);
    const created: DemoReminder = {
      id: nextId,
      medicineId: rMedId,
      userId: currentUser.id,
      time: rTime,
      label: rLabel.trim() || 'Scheduled Dose',
      active: rActive,
    };
    setStore((prev) => ({
      ...prev,
      reminders: [...prev.reminders, created],
      logs: [
        ...prev.logs,
        appendLog(currentUser.username, `Created reminder: ${created.time} - ${med?.name ?? 'Medicine'}`),
      ],
    }));
    setRemModalOpen(false);
    showNotice(`Reminder scheduled for ${created.time}.`);
  }

  function toggleReminder(r: DemoReminder) {
    setStore((prev) => ({
      ...prev,
      reminders: prev.reminders.map((item) => (item.id === r.id ? { ...item, active: !item.active } : item)),
    }));
  }

  function deleteReminder(r: DemoReminder) {
    setStore((prev) => ({
      ...prev,
      reminders: prev.reminders.filter((item) => item.id !== r.id),
    }));
  }

  function logDose(reminder: DemoReminder, status: 'TAKEN' | 'MISSED', remarks: string) {
    if (!currentUser) return;
    const med = medicineMap.get(reminder.medicineId);
    const nextId = store.history.reduce((max, h) => Math.max(max, h.id), 0) + 1;
    const entry: DemoHistory = {
      id: nextId,
      medicineId: reminder.medicineId,
      reminderId: reminder.id,
      userId: currentUser.id,
      doseDate: todayIso(0),
      scheduledTime: reminder.time,
      takenAt: status === 'TAKEN' ? nowStamp() : null,
      status,
      remarks,
    };
    setStore((prev) => ({
      ...prev,
      history: [...prev.history, entry],
      logs: [
        ...prev.logs,
        appendLog(currentUser.username, `Marked ${med?.name ?? 'Medicine'} (${reminder.time}) as ${status}`),
      ],
    }));
    showNotice(`Recorded ${med?.name ?? 'dose'} as ${status}.`);
  }

  function simulateAlert(reminder?: DemoReminder) {
    const target = reminder ?? todayReminders[0] ?? userReminders[0];
    if (!target) {
      showNotice('Add a reminder first to simulate a background alert.');
      return;
    }
    const med = medicineMap.get(target.medicineId);
    if (!med || !currentUser) return;
    setStore((prev) => ({
      ...prev,
      logs: [
        ...prev.logs,
        appendLog(currentUser.username, `Reminder triggered for ${med.name} at ${target.time}`),
      ],
    }));
    setActiveAlert({ reminder: target, medicine: med });
  }

  function handleExportCsv() {
    if (!currentUser) return;
    const header = 'HistoryID,DoseDate,ScheduledTime,MedicineName,Dosage,Status,TakenAt,Remarks';
    const rows = userHistory.map((h) => {
      const med = medicineMap.get(h.medicineId);
      return `${h.id},${h.doseDate},${h.scheduledTime},"${med?.name ?? 'Medicine'}","${med?.dosage ?? ''}",${h.status},${h.takenAt ?? ''},"${h.remarks}"`;
    });
    const csv = [header, ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `medicine_history_${currentUser.username}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    setStore((prev) => ({
      ...prev,
      logs: [...prev.logs, appendLog(currentUser.username, `Exported CSV report (${rows.length} rows)`)],
    }));
    showNotice('Exported medicine_history CSV report.');
  }

  function handleResetDemo() {
    const fresh = createSeedStore();
    setStore(fresh);
    setCurrentUserId(1);
    setAuthScreen('login');
    setActiveTab('today');
    showNotice('Demo data reset to initial seed state.');
  }

  if (!currentUser) {
    return (
      <div className="max-w-md mx-auto py-8">
        <div className="bg-white border border-slate-200 rounded-xl p-6">
          <div className="mb-6">
            <p className="text-xs font-medium text-teal-700 mb-1">
              {authScreen === 'login' ? 'Swing LoginFrame Simulation' : 'Swing RegisterFrame Simulation'}
            </p>
            <h2 className="text-xl font-semibold text-slate-900">
              {authScreen === 'login' ? 'Sign In to MedTrack' : 'Create MedTrack Account'}
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              {authScreen === 'login'
                ? 'Authenticate against the simulated MySQL users table.'
                : 'Register a new user with InputValidator checks.'}
            </p>
          </div>

          {authError && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-700">
              {authError}
            </div>
          )}
          {authSuccess && (
            <div className="mb-4 p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-xs text-emerald-700">
              {authSuccess}
            </div>
          )}

          {authScreen === 'login' ? (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">Username or Email</label>
                <input
                  type="text"
                  value={loginIdentifier}
                  onChange={(e) => setLoginIdentifier(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">Password</label>
                <input
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                />
              </div>
              <button
                type="submit"
                className="w-full py-2.5 px-4 bg-teal-700 hover:bg-teal-800 text-white text-sm font-medium rounded-lg transition-colors"
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => {
                  setAuthError('');
                  setAuthSuccess('');
                  setAuthScreen('register');
                }}
                className="w-full py-2 px-4 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-lg transition-colors"
              >
                New User? Create an Account
              </button>
              <p className="text-xs text-slate-500 text-center pt-2 font-mono">
                Demo: arjun_med / MedTrack@123
              </p>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name *</label>
                <input
                  type="text"
                  value={regName}
                  onChange={(e) => setRegName(e.target.value)}
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Username *</label>
                  <input
                    type="text"
                    value={regUsername}
                    onChange={(e) => setRegUsername(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Phone</label>
                  <input
                    type="text"
                    value={regPhone}
                    onChange={(e) => setRegPhone(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Email *</label>
                <input
                  type="email"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Password *</label>
                  <input
                    type="password"
                    value={regPassword}
                    onChange={(e) => setRegPassword(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Confirm *</label>
                  <input
                    type="password"
                    value={regConfirm}
                    onChange={(e) => setRegConfirm(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                  />
                </div>
              </div>
              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setAuthError('');
                    setAuthScreen('login');
                  }}
                  className="px-4 py-2 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg"
                >
                  Back
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2 px-4 bg-teal-700 hover:bg-teal-800 text-white text-sm font-medium rounded-lg"
                >
                  Register Account
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Window Context Header */}
      <div className="bg-white border border-slate-200 rounded-xl p-5 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500">
            <span>Interactive Swing Preview</span>
            <span aria-hidden="true">·</span>
            <span>User: {currentUser.fullName} (@{currentUser.username})</span>
            <span aria-hidden="true">·</span>
            <span>{currentUser.email}</span>
          </div>
          <h2 className="text-lg font-semibold text-slate-900 mt-1">
            Medicine Reminder &amp; Management Dashboard
          </h2>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => simulateAlert()}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-teal-800 bg-teal-50 hover:bg-teal-100 border border-teal-200 rounded-lg whitespace-nowrap shrink-0 transition-colors"
          >
            <Bell className="w-3.5 h-3.5" />
            Simulate Alert Popup
          </button>
          <button
            type="button"
            onClick={handleExportCsv}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 rounded-lg whitespace-nowrap shrink-0 transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            Export History CSV
          </button>
          <button
            type="button"
            onClick={handleResetDemo}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-slate-600 bg-white hover:bg-slate-50 border border-slate-200 rounded-lg whitespace-nowrap shrink-0 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Reset Demo
          </button>
          <button
            type="button"
            onClick={() => setCurrentUserId(null)}
            className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 border border-red-200 rounded-lg whitespace-nowrap shrink-0 transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" />
            Logout
          </button>
        </div>
      </div>

      {feedbackNotice && (
        <div className="px-4 py-2.5 bg-teal-50 border border-teal-200 rounded-lg text-xs font-medium text-teal-900 flex items-center justify-between">
          <span>{feedbackNotice}</span>
          <button type="button" onClick={() => setFeedbackNotice('')} className="text-teal-700 hover:text-teal-900">
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* KPI Strip */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white border border-slate-200 rounded-xl p-4">
          <p className="text-xs text-slate-500">Total Medicines</p>
          <p className="text-2xl font-semibold text-teal-700 mt-1 font-mono tabular-nums">
            {stats.totalMedicines}
          </p>
        </div>
        <div className="bg-white border border-slate-200 rounded-xl p-4">
          <p className="text-xs text-slate-500">Upcoming Reminders</p>
          <p className="text-2xl font-semibold text-blue-700 mt-1 font-mono tabular-nums">
            {stats.activeReminders}
          </p>
        </div>
        <div className="bg-white border border-slate-200 rounded-xl p-4">
          <p className="text-xs text-slate-500">Doses Taken</p>
          <p className="text-2xl font-semibold text-emerald-700 mt-1 font-mono tabular-nums">
            {stats.taken}
          </p>
        </div>
        <div className="bg-white border border-slate-200 rounded-xl p-4">
          <p className="text-xs text-slate-500">Doses Missed</p>
          <p className="text-2xl font-semibold text-red-700 mt-1 font-mono tabular-nums">
            {stats.missed}
          </p>
        </div>
      </div>

      {/* Main Workspace Tabs */}
      <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
        <div className="border-b border-slate-200 px-4 pt-3 flex items-center gap-2 overflow-x-auto">
          {(
            [
              ['today', "Today's Schedule"],
              ['medicines', 'Medicines'],
              ['reminders', 'Reminders'],
              ['history', 'History & Logs'],
            ] as const
          ).map(([key, label]) => (
            <button
              key={key}
              type="button"
              onClick={() => setActiveTab(key)}
              className={`px-3.5 py-2 text-xs font-medium border-b-2 transition-colors whitespace-nowrap shrink-0 ${
                activeTab === key
                  ? 'border-teal-700 text-teal-800'
                  : 'border-transparent text-slate-500 hover:text-slate-900'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="p-5">
          {activeTab === 'today' && (
            <div className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="text-sm font-semibold text-slate-900">
                    Active Reminders for Today ({todayIso(0)})
                  </h3>
                  <p className="text-xs text-slate-500">
                    Log doses directly or inspect polymorphic dosage guidance per medicine type.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={openAddReminder}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-teal-700 hover:bg-teal-800 text-white text-xs font-medium rounded-lg whitespace-nowrap shrink-0"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Set Reminder
                </button>
              </div>

              {todayReminders.length === 0 ? (
                <div className="py-10 text-center border border-dashed border-slate-200 rounded-lg">
                  <p className="text-sm text-slate-600">No active reminders scheduled for today.</p>
                  <button
                    type="button"
                    onClick={openAddReminder}
                    className="mt-3 px-3.5 py-1.5 bg-teal-700 text-white text-xs font-medium rounded-lg"
                  >
                    Create First Reminder
                  </button>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
                        <th className="py-2.5 pr-4">Time</th>
                        <th className="py-2.5 pr-4">Medicine</th>
                        <th className="py-2.5 pr-4">Dosage</th>
                        <th className="py-2.5 pr-4">Note</th>
                        <th className="py-2.5 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-sm">
                      {todayReminders.map((r) => {
                        const med = medicineMap.get(r.medicineId);
                        return (
                          <tr key={r.id} className="hover:bg-slate-50">
                            <td className="py-3 pr-4 font-mono tabular-nums font-medium text-slate-900">
                              {r.time}
                            </td>
                            <td className="py-3 pr-4 font-medium text-slate-900">
                              {med?.name ?? 'Medicine'}
                              <span className="ml-2 text-xs font-normal text-slate-500">
                                {med?.type}
                              </span>
                            </td>
                            <td className="py-3 pr-4 text-slate-600">{med?.dosage ?? '-'}</td>
                            <td className="py-3 pr-4 text-slate-600">{r.label}</td>
                            <td className="py-3 text-right">
                              <div className="inline-flex items-center gap-1.5">
                                {med && (
                                  <button
                                    type="button"
                                    onClick={() => setGuideMed(med)}
                                    className="px-2.5 py-1 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-md whitespace-nowrap shrink-0"
                                  >
                                    Dosage Guide
                                  </button>
                                )}
                                <button
                                  type="button"
                                  onClick={() => logDose(r, 'TAKEN', "Marked from Today's Schedule")}
                                  className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-700 rounded-md whitespace-nowrap shrink-0"
                                >
                                  <Check className="w-3 h-3" />
                                  Taken
                                </button>
                                <button
                                  type="button"
                                  onClick={() => logDose(r, 'MISSED', "Marked from Today's Schedule")}
                                  className="px-2.5 py-1 text-xs font-medium text-amber-800 bg-amber-100 hover:bg-amber-200 rounded-md whitespace-nowrap shrink-0"
                                >
                                  Missed
                                </button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {activeTab === 'medicines' && (
            <div className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                  <div className="relative">
                    <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="text"
                      placeholder="Search medicine name..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-8 pr-3 py-1.5 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-600"
                    />
                  </div>
                  <div className="flex items-center gap-1 p-0.5 bg-slate-100 rounded-lg">
                    {(['ALL', 'PRESCRIPTION', 'OTC'] as const).map((t) => (
                      <button
                        key={t}
                        type="button"
                        onClick={() => setTypeFilter(t)}
                        className={`px-2.5 py-1 text-xs font-medium rounded-md transition-colors whitespace-nowrap shrink-0 ${
                          typeFilter === t
                            ? 'bg-white text-slate-900 shadow-sm'
                            : 'text-slate-600 hover:text-slate-900'
                        }`}
                      >
                        {t}
                      </button>
                    ))}
                  </div>
                </div>

                <button
                  type="button"
                  onClick={openAddMedicine}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-teal-700 hover:bg-teal-800 text-white text-xs font-medium rounded-lg whitespace-nowrap shrink-0"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Add Medicine
                </button>
              </div>

              {filteredMedicines.length === 0 ? (
                <div className="py-10 text-center border border-dashed border-slate-200 rounded-lg">
                  <p className="text-sm text-slate-600">No medicines match your current filter.</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
                        <th className="py-2.5 pr-4">Name</th>
                        <th className="py-2.5 pr-4">Type</th>
                        <th className="py-2.5 pr-4">Dosage</th>
                        <th className="py-2.5 pr-4">Frequency</th>
                        <th className="py-2.5 pr-4">Window</th>
                        <th className="py-2.5 pr-4">Doctor / Category</th>
                        <th className="py-2.5 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-sm">
                      {filteredMedicines.map((m) => (
                        <tr key={m.id} className="hover:bg-slate-50">
                          <td className="py-3 pr-4 font-medium text-slate-900">{m.name}</td>
                          <td className="py-3 pr-4 text-xs font-mono text-slate-600">{m.type}</td>
                          <td className="py-3 pr-4 text-slate-700">{m.dosage}</td>
                          <td className="py-3 pr-4 text-slate-700">{m.frequency}</td>
                          <td className="py-3 pr-4 font-mono tabular-nums text-xs text-slate-600">
                            {m.startDate} to {m.endDate}
                          </td>
                          <td className="py-3 pr-4 text-xs text-slate-600">
                            {m.type === 'PRESCRIPTION'
                              ? m.doctorName || 'Prescription'
                              : m.purposeCategory || 'Over-the-Counter'}
                          </td>
                          <td className="py-3 text-right">
                            <div className="inline-flex items-center gap-1">
                              <button
                                type="button"
                                onClick={() => openEditMedicine(m)}
                                className="p-1.5 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-md"
                                title="Edit medicine"
                              >
                                <Edit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                type="button"
                                onClick={() => handleDeleteMedicine(m)}
                                className="p-1.5 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-md"
                                title="Delete medicine"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {activeTab === 'reminders' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between gap-3">
                <p className="text-xs text-slate-500">
                  Background ReminderSchedulerThread checks active reminders every 15 seconds.
                </p>
                <button
                  type="button"
                  onClick={openAddReminder}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-teal-700 hover:bg-teal-800 text-white text-xs font-medium rounded-lg whitespace-nowrap shrink-0"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Set Reminder
                </button>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
                      <th className="py-2.5 pr-4">Time</th>
                      <th className="py-2.5 pr-4">Medicine</th>
                      <th className="py-2.5 pr-4">Label</th>
                      <th className="py-2.5 pr-4">Status</th>
                      <th className="py-2.5 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 text-sm">
                    {userReminders.map((r) => {
                      const med = medicineMap.get(r.medicineId);
                      return (
                        <tr key={r.id} className="hover:bg-slate-50">
                          <td className="py-3 pr-4 font-mono tabular-nums font-medium text-slate-900">
                            {r.time}
                          </td>
                          <td className="py-3 pr-4 font-medium text-slate-900">
                            {med?.name ?? 'Medicine'}
                          </td>
                          <td className="py-3 pr-4 text-slate-600">{r.label}</td>
                          <td className="py-3 pr-4 text-xs font-medium">
                            <span className={r.active ? 'text-emerald-700' : 'text-slate-400'}>
                              {r.active ? 'ACTIVE' : 'PAUSED'}
                            </span>
                          </td>
                          <td className="py-3 text-right">
                            <div className="inline-flex items-center gap-1.5">
                              <button
                                type="button"
                                onClick={() => simulateAlert(r)}
                                className="px-2.5 py-1 text-xs font-medium text-teal-800 bg-teal-50 hover:bg-teal-100 rounded-md whitespace-nowrap shrink-0"
                              >
                                Test Alert
                              </button>
                              <button
                                type="button"
                                onClick={() => toggleReminder(r)}
                                className="px-2.5 py-1 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-md whitespace-nowrap shrink-0"
                              >
                                {r.active ? 'Pause' : 'Resume'}
                              </button>
                              <button
                                type="button"
                                onClick={() => deleteReminder(r)}
                                className="p-1.5 text-red-600 hover:bg-red-50 rounded-md"
                                title="Delete reminder"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'history' && (
            <div className="space-y-6">
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500">
                      <th className="py-2.5 pr-4">Date</th>
                      <th className="py-2.5 pr-4">Scheduled</th>
                      <th className="py-2.5 pr-4">Medicine</th>
                      <th className="py-2.5 pr-4">Status</th>
                      <th className="py-2.5 pr-4">Remarks</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 text-sm">
                    {userHistory.map((h) => {
                      const med = medicineMap.get(h.medicineId);
                      return (
                        <tr key={h.id} className="hover:bg-slate-50">
                          <td className="py-2.5 pr-4 font-mono tabular-nums text-xs text-slate-700">
                            {h.doseDate}
                          </td>
                          <td className="py-2.5 pr-4 font-mono tabular-nums text-xs text-slate-700">
                            {h.scheduledTime}
                          </td>
                          <td className="py-2.5 pr-4 font-medium text-slate-900">
                            {med?.name ?? 'Medicine'} ({med?.dosage ?? '-'})
                          </td>
                          <td className="py-2.5 pr-4 text-xs font-semibold">
                            <span className={h.status === 'TAKEN' ? 'text-emerald-700' : 'text-red-700'}>
                              {h.status}
                            </span>
                          </td>
                          <td className="py-2.5 pr-4 text-xs text-slate-600">{h.remarks}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              <div className="border-t border-slate-200 pt-4">
                <div className="flex items-center gap-2 text-xs font-semibold text-slate-700 mb-2">
                  <FileText className="w-3.5 h-3.5 text-slate-500" />
                  <span>FileReportService Activity Log (logs/reminder_activity.log)</span>
                </div>
                <pre className="bg-slate-900 text-slate-100 rounded-lg p-3.5 text-xs font-mono overflow-x-auto max-h-44">
                  {store.logs.slice(-15).join('\n')}
                </pre>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Add / Edit Medicine Modal */}
      {medModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-xl max-w-lg w-full p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-semibold text-slate-900">
                {editingMed ? 'Edit Medicine' : 'Add New Medicine'}
              </h3>
              <button type="button" onClick={() => setMedModalOpen(false)} className="text-slate-400 hover:text-slate-700">
                <X className="w-4 h-4" />
              </button>
            </div>
            {modalError && (
              <div className="p-2.5 rounded-lg bg-red-50 border border-red-200 text-xs text-red-700">
                {modalError}
              </div>
            )}
            <form onSubmit={handleSaveMedicine} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Medicine Name *</label>
                  <input
                    type="text"
                    value={mName}
                    onChange={(e) => setMName(e.target.value)}
                    placeholder="e.g., Metformin"
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Type (Polymorphism) *</label>
                  <select
                    value={mType}
                    onChange={(e) => setMType(e.target.value as 'PRESCRIPTION' | 'OTC')}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg bg-white"
                  >
                    <option value="PRESCRIPTION">PRESCRIPTION</option>
                    <option value="OTC">OTC</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Dosage *</label>
                  <input
                    type="text"
                    value={mDosage}
                    onChange={(e) => setMDosage(e.target.value)}
                    placeholder="e.g., 500 mg"
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Frequency *</label>
                  <select
                    value={mFreq}
                    onChange={(e) => setMFreq(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg bg-white"
                  >
                    <option>Once Daily</option>
                    <option>Twice Daily</option>
                    <option>Three Times Daily</option>
                    <option>Every 8 Hours</option>
                    <option>Before Sleep</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Start Date *</label>
                  <input
                    type="date"
                    value={mStart}
                    onChange={(e) => setMStart(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">End Date *</label>
                  <input
                    type="date"
                    value={mEnd}
                    onChange={(e) => setMEnd(e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg font-mono"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {mType === 'PRESCRIPTION' ? 'Doctor Name' : 'OTC Category'}
                </label>
                <input
                  type="text"
                  value={mDetail}
                  onChange={(e) => setMDetail(e.target.value)}
                  placeholder={mType === 'PRESCRIPTION' ? 'e.g., Dr. Kavita Rao' : 'e.g., Pain Relief'}
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Notes</label>
                <input
                  type="text"
                  value={mNotes}
                  onChange={(e) => setMNotes(e.target.value)}
                  placeholder="Optional instructions"
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setMedModalOpen(false)}
                  className="px-4 py-2 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-medium text-white bg-teal-700 hover:bg-teal-800 rounded-lg"
                >
                  {editingMed ? 'Update Medicine' : 'Save Medicine'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Set Reminder Modal */}
      {remModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-semibold text-slate-900">Set Medicine Reminder</h3>
              <button type="button" onClick={() => setRemModalOpen(false)} className="text-slate-400 hover:text-slate-700">
                <X className="w-4 h-4" />
              </button>
            </div>
            {modalError && (
              <div className="p-2.5 rounded-lg bg-red-50 border border-red-200 text-xs text-red-700">
                {modalError}
              </div>
            )}
            <form onSubmit={handleSaveReminder} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Select Medicine *</label>
                <select
                  value={rMedId}
                  onChange={(e) => setRMedId(Number(e.target.value))}
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg bg-white"
                >
                  {userMedicines.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name} ({m.dosage})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Time (24-hour HH:mm) *</label>
                <input
                  type="time"
                  value={rTime}
                  onChange={(e) => setRTime(e.target.value)}
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg font-mono"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Reminder Note</label>
                <input
                  type="text"
                  value={rLabel}
                  onChange={(e) => setRLabel(e.target.value)}
                  className="w-full px-3 py-1.5 text-sm border border-slate-300 rounded-lg"
                />
              </div>
              <label className="flex items-center gap-2 text-xs text-slate-700">
                <input
                  type="checkbox"
                  checked={rActive}
                  onChange={(e) => setRActive(e.target.checked)}
                />
                Enable reminder immediately
              </label>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setRemModalOpen(false)}
                  className="px-4 py-2 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-medium text-white bg-teal-700 hover:bg-teal-800 rounded-lg"
                >
                  Save Reminder
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Polymorphic Dosage Instructions Modal */}
      {guideMed && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-xl max-w-md w-full p-6 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-semibold text-slate-900">
                {guideMed.name} — Dosage Guide
              </h3>
              <button type="button" onClick={() => setGuideMed(null)} className="text-slate-400 hover:text-slate-700">
                <X className="w-4 h-4" />
              </button>
            </div>
            <p className="text-xs font-mono text-slate-500">
              Runtime Polymorphism: {guideMed.type === 'PRESCRIPTION' ? 'PrescriptionMedicine' : 'OTCMedicine'}.getDosageInstructions()
            </p>
            <p className="text-sm text-slate-700 leading-relaxed bg-slate-50 border border-slate-200 rounded-lg p-3.5">
              {getDosageInstructions(guideMed)}
            </p>
            {guideMed.notes && (
              <p className="text-xs text-slate-600">Notes: {guideMed.notes}</p>
            )}
            <div className="flex justify-end pt-1">
              <button
                type="button"
                onClick={() => setGuideMed(null)}
                className="px-4 py-1.5 text-xs font-medium text-white bg-teal-700 rounded-lg"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Simulated Multithreaded Reminder Alert Dialog */}
      {activeAlert && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-xl max-w-md w-full p-6 space-y-4 shadow-lg">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-xs font-mono text-teal-700">
                  ReminderSchedulerThread → ReminderListener
                </p>
                <h3 className="text-lg font-semibold text-slate-900 mt-0.5">
                  Time to Take Your Medicine
                </h3>
              </div>
              <button type="button" onClick={() => setActiveAlert(null)} className="text-slate-400 hover:text-slate-700">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-lg p-3.5 space-y-1.5 text-sm">
              <p className="font-semibold text-slate-900">
                {activeAlert.medicine.name} ({activeAlert.medicine.dosage})
              </p>
              <p className="text-xs text-slate-600 font-mono">
                Scheduled: {activeAlert.reminder.time} · {activeAlert.reminder.label}
              </p>
              <p className="text-xs text-slate-600 pt-1">
                {getDosageInstructions(activeAlert.medicine)}
              </p>
            </div>

            <div className="flex flex-wrap justify-end gap-2">
              <button
                type="button"
                onClick={() => setActiveAlert(null)}
                className="px-3.5 py-2 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg"
              >
                Dismiss
              </button>
              <button
                type="button"
                onClick={() => {
                  logDose(activeAlert.reminder, 'MISSED', 'Marked missed via reminder popup');
                  setActiveAlert(null);
                }}
                className="px-3.5 py-2 text-xs font-medium text-amber-800 bg-amber-100 hover:bg-amber-200 rounded-lg"
              >
                Mark as Missed
              </button>
              <button
                type="button"
                onClick={() => {
                  logDose(activeAlert.reminder, 'TAKEN', 'Confirmed via reminder popup');
                  setActiveAlert(null);
                }}
                className="px-3.5 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg"
              >
                Mark as Taken
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
